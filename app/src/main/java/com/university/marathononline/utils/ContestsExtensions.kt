package com.university.marathononline.utils

import com.university.marathononline.data.models.*
import java.time.LocalDateTime

/**
 * Extension functions để xử lý null safety và validation cho Contest models
 */

/**
 * Kiểm tra xem contest có hợp lệ không
 */
fun Contest.isValid(): Boolean {
    return this.id != null && this.name?.isNotBlank() == true
}

/**
 * Kiểm tra xem registration có payment hợp lệ không
 */
fun Registration.hasValidPayment(): Boolean {
    return this.payment != null && this.payment.status != null
}

/**
 * Lấy payment status an toàn
 */
fun Registration.getPaymentStatusSafely(): EPaymentStatus? {
    return this.payment?.status
}

/**
 * Kiểm tra xem contest có đang active không
 */
fun Contest.isActive(): Boolean {
    return this.status == EContestStatus.ACTIVE
}

/**
 * Kiểm tra xem contest có đã kết thúc không
 */
fun Contest.isFinished(): Boolean {
    return this.status == EContestStatus.FINISHED || this.status == EContestStatus.COMPLETED
}

fun Contest.isRegistrationDeadlinePassed(): Boolean {
    return this.registrationDeadline?.let { deadline ->
        try {
            val deadlineTime = DateUtils.convertStringToLocalDateTime(deadline)
            val now = LocalDateTime.now()
            val isPassed = deadlineTime.isBefore(now)

            isPassed
        } catch (e: Exception) {
            false
        }
    } ?: false
}

fun Contest.isMaxRegistrationsReached(): Boolean {
    return this.maxMembers != null &&
            this.maxMembers != 0 &&
            this.maxMembers!! <= (this.registrations?.size ?: 0)
}

/**
 * Kiểm tra xem contest có chưa bắt đầu không
 */
fun Contest.hasNotStarted(): Boolean {
    return this.startDate?.let { startDate ->
        try {
            DateUtils.convertStringToLocalDateTime(startDate).isAfter(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    } ?: false
}

/**
 * Lấy tổng số đăng ký hiện tại
 */
fun Contest.getCurrentRegistrationCount(): Int {
    return this.registrations?.size ?: 0
}

/**
 * Lấy tổng distance đã chạy của registration
 */
fun Registration.getTotalDistance(): Double {
    return this.records
        ?.filter { it.approval!!.approvalStatus != ERecordApprovalStatus.REJECTED }
        ?.sumOf { it.distance } ?: 0.0
}

/**
 * Kiểm tra registration có bị block không
 */
fun Registration.isBlocked(): Boolean {
    return this.status == ERegistrationStatus.BLOCK
}

/**
 * Kiểm tra registration có completed không
 */
fun Registration.isCompleted(): Boolean {
    return this.status == ERegistrationStatus.COMPLETED
}

/**
 * Kiểm tra registration có active không
 */
fun Registration.isActive(): Boolean {
    return this.status == ERegistrationStatus.ACTIVE
}