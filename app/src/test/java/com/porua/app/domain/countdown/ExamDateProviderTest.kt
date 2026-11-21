package com.porua.app.domain.countdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExamDateProviderTest {

    private class FakeDateProvider(private val fixedDate: LocalDate) : DateProvider {
        override fun today(): LocalDate = fixedDate
    }

    @Test
    fun testCountdownBeforeApril() {
        // Test date: March 15, 2027 (17 days before April 1, 2027)
        val fakeClock = FakeDateProvider(LocalDate.of(2027, 3, 15))
        val result = ExamDateProvider.calculateCountdown(2027, fakeClock)

        assertTrue("Expected Upcoming result", result is CountdownResult.Upcoming)
        val upcoming = result as CountdownResult.Upcoming
        assertEquals(17L, upcoming.daysRemaining)
        assertEquals(2027, upcoming.examYear)
        assertEquals("HSC 2027 • April", upcoming.formattedTarget)
    }

    @Test
    fun testCountdownOnAprilFirst() {
        // Test date: April 1, 2027 (Exam day)
        val fakeClock = FakeDateProvider(LocalDate.of(2027, 4, 1))
        val result = ExamDateProvider.calculateCountdown(2027, fakeClock)

        assertTrue("Expected Today result", result is CountdownResult.Today)
        val today = result as CountdownResult.Today
        assertEquals(2027, today.examYear)
    }

    @Test
    fun testCountdownAfterAprilFirst() {
        // Test date: April 2, 2027 (Exam date has passed)
        val fakeClock = FakeDateProvider(LocalDate.of(2027, 4, 2))
        val result = ExamDateProvider.calculateCountdown(2027, fakeClock)

        assertTrue("Expected Passed result (no negative numbers)", result is CountdownResult.Passed)
        val passed = result as CountdownResult.Passed
        assertEquals(2027, passed.examYear)
    }

    @Test
    fun testDynamicAvailableYearsBeforeApril() {
        // On January 10, 2027 (before April 1, 2027), current year 2027 should be offered
        val fakeClock = FakeDateProvider(LocalDate.of(2027, 1, 10))
        val years = ExamDateProvider.getAvailableExamYears(fakeClock)

        assertEquals(listOf(2027, 2028, 2029), years)
    }

    @Test
    fun testDynamicAvailableYearsAfterApril() {
        // On April 2, 2027 (after April 1, 2027), 2027 has passed, so offer next 3 years
        val fakeClock = FakeDateProvider(LocalDate.of(2027, 4, 2))
        val years = ExamDateProvider.getAvailableExamYears(fakeClock)

        assertEquals(listOf(2028, 2029, 2030), years)
    }
}
