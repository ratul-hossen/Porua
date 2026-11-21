package com.porua.app.domain.countdown

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

interface DateProvider {
    fun today(): LocalDate
}

class DefaultDateProvider : DateProvider {
    override fun today(): LocalDate = LocalDate.now()
}

sealed class CountdownResult {
    data class Upcoming(
        val daysRemaining: Long,
        val examYear: Int,
        val examDate: LocalDate,
        val formattedTarget: String
    ) : CountdownResult()

    data class Today(
        val examYear: Int,
        val examDate: LocalDate
    ) : CountdownResult()

    data class Passed(
        val examYear: Int,
        val examDate: LocalDate
    ) : CountdownResult()
}

object ExamDateProvider {
    // Configurable board exam default date: 1st of April
    const val DEFAULT_EXAM_MONTH: Int = 4
    const val DEFAULT_EXAM_DAY: Int = 1

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)

    fun getExamDate(year: Int): LocalDate =
        LocalDate.of(year, DEFAULT_EXAM_MONTH, DEFAULT_EXAM_DAY)

    fun formatExamDate(year: Int): String =
        getExamDate(year).format(DATE_FORMATTER)

    /**
     * HSC exams in Bangladesh are held every April.
     * If today's date is before the exam date of the current year, include the current year;
     * otherwise start from next year. Offers the next 3 years in total.
     */
    fun getAvailableExamYears(dateProvider: DateProvider = DefaultDateProvider()): List<Int> {
        val today = dateProvider.today()
        val currentYearExamDate = getExamDate(today.year)
        val startYear = if (today.isBefore(currentYearExamDate)) today.year else today.year + 1
        return listOf(startYear, startYear + 1, startYear + 2)
    }

    /**
     * Calculates days left from today to the exam date.
     * Returns CountdownResult (Upcoming, Today, or Passed).
     * Never returns negative days.
     */
    fun calculateCountdown(
        examYear: Int,
        dateProvider: DateProvider = DefaultDateProvider()
    ): CountdownResult {
        val today = dateProvider.today()
        val examDate = getExamDate(examYear)
        val days = ChronoUnit.DAYS.between(today, examDate)
        val monthName = examDate.format(MONTH_FORMATTER)

        return when {
            days > 0 -> CountdownResult.Upcoming(
                daysRemaining = days,
                examYear = examYear,
                examDate = examDate,
                formattedTarget = "HSC $examYear • $monthName"
            )
            days == 0L -> CountdownResult.Today(
                examYear = examYear,
                examDate = examDate
            )
            else -> CountdownResult.Passed(
                examYear = examYear,
                examDate = examDate
            )
        }
    }
}
