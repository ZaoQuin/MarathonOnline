package com.university.marathononline.data.models

data class TrainingDay (
    var id: Long,
    var week: Int,
    var dayOfWeek: Int,
    var session: TrainingSession,
    var records: List<Record>,
    var status: ETraingDayStatus
)

enum class ETraingDayStatus(val value: String) {
    ACTIVE("Đang thực hiện"),
    COMPLETED("Hoàn thành"),
    MISSED("Bỏ lỡ");
    override fun toString(): String = value
}