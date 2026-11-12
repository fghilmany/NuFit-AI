package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.fake.FakeSyncRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DiscardLocalDataAndSyncTest {

    @Test
    fun `given wipe succeeds when invoked then clears local data then syncs`() = runTest {
        val syncRepository = FakeSyncRepository()
        val discardLocalDataAndSync = DiscardLocalDataAndSync(syncRepository)

        val result = discardLocalDataAndSync()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, syncRepository.clearAllLocalDataCallCount)
        assertEquals(1, syncRepository.syncNowCallCount)
    }

    @Test
    fun `given wipe fails when invoked then does not sync`() = runTest {
        val syncRepository = FakeSyncRepository().apply { clearAllLocalDataResult = AppResult.Error(Failure.Database("boom")) }
        val discardLocalDataAndSync = DiscardLocalDataAndSync(syncRepository)

        val result = discardLocalDataAndSync()

        assertEquals(AppResult.Error(Failure.Database("boom")), result)
        assertEquals(0, syncRepository.syncNowCallCount)
    }
}
