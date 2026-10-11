package com.porua.app.domain.sm2

import kotlin.math.max

/**
 * SuperMemo 2 (SM-2) Spaced Repetition Algorithm.
 * Implemented on-device for Porua's offline-first architecture.
 */
enum class Sm2Rating(val quality: Int, val label: String, val intervalDescription: String) {
    AGAIN(quality = 0, label = "Again", intervalDescription = "< 1 min"),
    HARD(quality = 3, label = "Hard", intervalDescription = "6 min"),
    GOOD(quality = 4, label = "Good", intervalDescription = "1 day"),
    EASY(quality = 5, label = "Easy", intervalDescription = "4 days")
}

data class Sm2Result(
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Float,
    val nextReviewTimestamp: Long
)

object Sm2Algorithm {

    /**
     * Calculates the next review interval and new ease factor according to SM-2 formula.
     *
     * @param rating User feedback rating (AGAIN, HARD, GOOD, EASY)
     * @param currentRepetitions Consecutive correct reviews
     * @param currentIntervalDays Previous interval in days
     * @param currentEaseFactor Difficulty factor (default starts at 2.5)
     */
    fun calculateNextReview(
        rating: Sm2Rating,
        currentRepetitions: Int,
        currentIntervalDays: Int,
        currentEaseFactor: Float,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Sm2Result {
        val q = rating.quality

        // Calculate new Ease Factor:
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        var newEf = currentEaseFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        if (newEf < 1.3f) newEf = 1.3f // Minimum EF threshold

        val newRepetitions: Int
        val newIntervalDays: Int

        if (q < 3) {
            // Failed recall (Again) -> reset streak
            newRepetitions = 0
            newIntervalDays = 1
        } else {
            // Successful recall
            newRepetitions = currentRepetitions + 1
            newIntervalDays = when (newRepetitions) {
                1 -> 1
                2 -> if (rating == Sm2Rating.EASY) 4 else 2
                else -> max(1, (currentIntervalDays * newEf).toInt())
            }
        }

        val millisInDay = 24L * 60 * 60 * 1000
        val nextReview = currentTimeMillis + (newIntervalDays * millisInDay)

        return Sm2Result(
            repetitions = newRepetitions,
            intervalDays = newIntervalDays,
            easeFactor = newEf,
            nextReviewTimestamp = nextReview
        )
    }
}
