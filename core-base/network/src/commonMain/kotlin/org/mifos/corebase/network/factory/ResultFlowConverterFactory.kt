/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.corebase.network.factory

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.Converter
import de.jensklingenberg.ktorfit.converter.TypeData
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import org.mifos.corebase.network.RemoteError
import org.mifos.corebase.network.Result

/**
 * A custom [Converter.Factory] implementation for Ktorfit that handles HTTP responses returning
 * a [Flow] of [Result] objects.
 *
 * This factory is useful for APIs that expose streaming or reactive data (e.g., via Kotlin [Flow]),
 * while providing unified error handling using a sealed [Result] type.
 *
 * It supports:
 * - HTTP 2xx responses converted into [Result.Success]
 * - Known HTTP error codes mapped to [RemoteError] via [Result.Error]
 * - Serialization failures and unknown exceptions
 *
 * Example expected return type:
 * ```kotlin
 * @GET("items")
 * fun getItems(): Flow<Result<List<Item>, RemoteError>>
 * ```
 */
class ResultFlowConverterFactory : Converter.Factory {

    /**
     * Returns a [Converter.ResponseConverter] if the expected return type is a `Flow<Result<...>>`.
     *
     * @param typeData Metadata about the return type (used to extract type inside Flow/Result)
     * @param ktorfit The Ktorfit instance that is requesting the converter
     * @return A response converter capable of wrapping an HTTP response as a `Flow<Result<D, RemoteError>>`
     */
    override fun responseConverter(
        typeData: TypeData,
        ktorfit: Ktorfit,
    ): Converter.ResponseConverter<HttpResponse, *>? {
        // Only handle Flow<Result<...>>
        if (typeData.typeInfo.type == Flow::class &&
            typeData.typeArgs.first().typeInfo.type == Result::class
        ) {
            val innerType = typeData.typeArgs.first().typeArgs.first().typeInfo
            return object :
                Converter.ResponseConverter<HttpResponse, Flow<Result<Any, RemoteError>>> {

                /**
                 * Converts a suspending HTTP call to a [Flow] of [Result],
                 * handling response status and deserialization.
                 *
                 * @param getResponse A suspend lambda that returns an [HttpResponse]
                 * @return A [Flow] emitting a [Result] indicating either success or error.
                 */
                override fun convert(getResponse: suspend () -> HttpResponse): Flow<Result<Any, RemoteError>> =
                    flow {
                        val response = try {
                            getResponse()
                        } catch (e: Exception) {
                            println("Failure: " + e.message)
                            emit(Result.Error(RemoteError.UNKNOWN))
                            return@flow
                        }

                        when (response.status.value) {
                            in 200..299 -> {
                                try {
                                    val data = response.body(innerType) as Any
                                    emit(Result.Success(data))
                                } catch (e: SerializationException) {
                                    println("Serialization error: ${e.message}")
                                    emit(Result.Error(RemoteError.SERIALIZATION))
                                }
                            }

                            400 -> emit(Result.Error(RemoteError.BAD_REQUEST))
                            401 -> emit(Result.Error(RemoteError.UNAUTHORIZED))
                            404 -> emit(Result.Error(RemoteError.NOT_FOUND))
                            408 -> emit(Result.Error(RemoteError.REQUEST_TIMEOUT))
                            429 -> emit(Result.Error(RemoteError.TOO_MANY_REQUESTS))
                            in 500..599 -> emit(Result.Error(RemoteError.SERVER))
                            else -> {
                                println("Status code ${response.status.value}")
                                emit(Result.Error(RemoteError.UNKNOWN))
                            }
                        }
                    }
            }
        }
        return null
    }
}
