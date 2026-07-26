package com.fghilmany.nufitai.usecase.profile

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.fake.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SaveProfileBasicsTest {

    @Test
    fun `given valid age and gender when saved then getProfileBasics returns it`() = runTest {
        val repository = FakeProfileRepository()
        val saveProfileBasics = SaveProfileBasics(repository)
        val getProfileBasics = GetProfileBasics(repository)

        val saveResult = saveProfileBasics(28, Gender.FEMALE)
        assertEquals(AppResult.Success(Unit), saveResult)

        val stored = (getProfileBasics() as AppResult.Success).data
        assertNotNull(stored)
        assertEquals(28, stored.age)
        assertEquals(Gender.FEMALE, stored.gender)
    }
}
