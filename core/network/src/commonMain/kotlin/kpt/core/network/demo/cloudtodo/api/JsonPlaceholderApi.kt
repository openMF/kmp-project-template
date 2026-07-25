/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.demo.cloudtodo.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import kpt.core.network.demo.cloudtodo.dto.TodoDto

/**
 * jsonplaceholder `/todos` — a free WRITABLE demo REST API (POST/PUT accepted, echoed back).
 * The template's other demo APIs (CoinGecko/FRED/WorldBank/Frankfurter) are read-only, so this
 * is the only endpoint that can showcase the Store5 MUTABLE write archetype end-to-end.
 * Ktorfit-generated impl — raw DTO returns, no NetworkResult (matches the demo pipeline).
 */
interface JsonPlaceholderApi {
    @GET("todos/{id}")
    suspend fun getTodo(@Path("id") id: Int): TodoDto

    /** Write-back (`PUT /todos/{id}`) — jsonplaceholder echoes the body as if persisted. */
    @PUT("todos/{id}")
    suspend fun updateTodo(@Path("id") id: Int, @Body todo: TodoDto): TodoDto
}
