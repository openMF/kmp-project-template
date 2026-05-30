package org.mifos.core.data.infra

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SynchronizerExtensionsTest {
    @Test
    fun changeListSync_partitionsAndBumpsVersion() = runTest {
        val sync = InMemorySynchronizer(initial = ChangeListVersions(mapOf("topics" to 3L)))
        val changes = listOf(
            FakeChange(id = "t1", changeListVersion = 4, isDelete = false),
            FakeChange(id = "t2", changeListVersion = 5, isDelete = false),
            FakeChange(id = "t4", changeListVersion = 6, isDelete = true),
            FakeChange(id = "t3", changeListVersion = 7, isDelete = false),
        )
        val deleted = mutableListOf<String>()
        val updated = mutableListOf<String>()
        val ok = sync.changeListSync<FakeChange>(
            versionReader = { (it.versions["topics"] ?: 0L).toInt() },
            changeListFetcher = { since -> changes.filter { it.changeListVersion > since } },
            versionUpdater = { latest -> copy(versions = versions + ("topics" to latest.toLong())) },
            modelDeleter = { ids -> deleted += ids },
            modelUpdater = { ids -> updated += ids },
        )
        assertTrue(ok)
        assertEquals(listOf("t4"), deleted)
        assertEquals(listOf("t1", "t2", "t3"), updated)
        assertEquals(7L, sync.getChangeListVersions().versions["topics"])
    }

    @Test
    fun snapshotSync_bumpsToEpochSeconds_andRunsFetcher() = runTest {
        val sync = InMemorySynchronizer()
        var fetcherInvoked = false
        val before = kotlinx.datetime.Clock.System.now().epochSeconds
        val ok = sync.snapshotSync(name = "currency-rates") { fetcherInvoked = true }
        val after = kotlinx.datetime.Clock.System.now().epochSeconds
        assertTrue(ok)
        assertTrue(fetcherInvoked)
        val stored = sync.getChangeListVersions().versions["currency-rates"]!!
        assertTrue(stored in before..after)
    }
}

private data class FakeChange(
    override val id: String,
    override val changeListVersion: Int,
    override val isDelete: Boolean,
) : NetworkChange

private class InMemorySynchronizer(initial: ChangeListVersions = ChangeListVersions()) : Synchronizer {
    private var state = initial
    override suspend fun getChangeListVersions() = state
    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        state = state.update()
    }
}
