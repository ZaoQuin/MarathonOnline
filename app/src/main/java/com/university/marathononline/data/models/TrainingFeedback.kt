package com.university.marathononline.data.models

data class TrainingFeedback (
    var id: Long,
    var difficultyRating: EDifficultyRating,
    var feelingRating: EFeelingRating,
    var notes: String,
)

enum class EDifficultyRating(val value: String) {
    VERY_EASY("Rất dễ"),
    EASY("Dễ"),
    MODERATE("Bình thường"),
    HARD("Khó"),
    VERY_HARD("Rất khó")
}

enum class EFeelingRating(val value: String) {
    EXCELLENT("Tuyệt vời"),
    GOOD("Tốt"),
    OKAY("Bình thường"),
    TIRED("Mệt"),
    EXHAUSTED("Kiệt sức")
}