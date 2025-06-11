package com.university.marathononline.utils

import com.university.marathononline.data.models.*

class ContestUserStatusManager(
    private val contest: Contest,
    private val userRegistration: Registration? = null
) {

    enum class UserContestStatus {
        NOT_REGISTERED,           // Chưa đăng ký
        REGISTERED_UNPAID,        // Đã đăng ký nhưng chưa thanh toán
        PAYMENT_FAILED,           // Thanh toán thất bại
        PAYMENT_PENDING,          // Chờ thanh toán
        REGISTERED_ACTIVE,        // Đã đăng ký và đang hoạt động
        REGISTERED_BLOCKED,       // Đã đăng ký nhưng bị chặn
        REGISTRATION_COMPLETED,   // Hoàn thành cuộc thi
        CONTEST_EXPIRED,         // Contest đã hết hạn
        REGISTRATION_FULL,       // Hết slot đăng ký
        REGISTRATION_CLOSED      // Hết hạn đăng ký
    }

    data class ContestDisplayState(
        val userStatus: UserContestStatus,
        val canRegister: Boolean,
        val canRecord: Boolean,
        val showLeaderboard: Boolean,
        val showProgress: Boolean,
        val registerButtonText: String,
        val recordButtonText: String,
        val registerButtonEnabled: Boolean,
        val recordButtonEnabled: Boolean,
        val statusMessage: String? = null
    )

    fun getUserContestStatus(): UserContestStatus {

        if (!contest.isValid()) {
            return UserContestStatus.NOT_REGISTERED
        }

        if (userRegistration == null) {
            when (contest.status) {
                EContestStatus.PENDING -> return UserContestStatus.NOT_REGISTERED
                EContestStatus.COMPLETED, EContestStatus.FINISHED -> {
                    return UserContestStatus.CONTEST_EXPIRED
                }
                null -> return UserContestStatus.NOT_REGISTERED
                EContestStatus.ACTIVE -> {
                    return when {
                        contest.isRegistrationDeadlinePassed() -> UserContestStatus.REGISTRATION_CLOSED
                        contest.isMaxRegistrationsReached() -> UserContestStatus.REGISTRATION_FULL
                        else -> UserContestStatus.NOT_REGISTERED
                    }
                }
                else ->  UserContestStatus.NOT_REGISTERED
            }
        }

        val paymentStatus = userRegistration!!.getPaymentStatusSafely()

        return when {
            paymentStatus == null -> {
                UserContestStatus.REGISTERED_UNPAID
            }
            paymentStatus == EPaymentStatus.FAILED -> UserContestStatus.PAYMENT_FAILED
            paymentStatus == EPaymentStatus.PENDING -> UserContestStatus.PAYMENT_PENDING
            paymentStatus == EPaymentStatus.SUCCESS -> {
                when {
                    userRegistration.isBlocked() -> UserContestStatus.REGISTERED_BLOCKED
                    userRegistration.isCompleted() -> UserContestStatus.REGISTRATION_COMPLETED
                    userRegistration.isActive() -> UserContestStatus.REGISTERED_ACTIVE
                    else -> UserContestStatus.REGISTERED_UNPAID
                }
            }
            else -> UserContestStatus.REGISTERED_UNPAID
        }
    }

    fun getDisplayState(): ContestDisplayState {
        val userStatus = getUserContestStatus()
        val contestHasntStarted = contest.hasNotStarted()

        return when (userStatus) {
            UserContestStatus.NOT_REGISTERED -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = true,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = false,
                registerButtonText = "Đăng ký tham gia",
                recordButtonText = "Ghi nhận thành tích",
                registerButtonEnabled = true,
                recordButtonEnabled = false
            )

            UserContestStatus.REGISTERED_UNPAID -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = false,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = false,
                registerButtonText = "Chưa thanh toán",
                recordButtonText = "Ghi nhận thành tích",
                registerButtonEnabled = true,
                recordButtonEnabled = false,
                statusMessage = "Bạn đã đăng ký nhưng chưa hoàn tất thanh toán. Vui lòng thanh toán để tham gia cuộc thi."
            )

            UserContestStatus.PAYMENT_FAILED -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = true,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = false,
                registerButtonText = "Thanh toán lại",
                recordButtonText = "Ghi nhận thành tích",
                registerButtonEnabled = true,
                recordButtonEnabled = false,
                statusMessage = "Thanh toán thất bại. Vui lòng thử lại."
            )

            UserContestStatus.PAYMENT_PENDING -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = true,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = false,
                registerButtonText = "Chờ thanh toán",
                recordButtonText = "Ghi nhận thành tích",
                registerButtonEnabled = true,
                recordButtonEnabled = false,
                statusMessage = "Bạn đã đăng ký và đang chờ thanh toán. Vui lòng hoàn tất thanh toán để tham gia cuộc thi."
            )

            UserContestStatus.REGISTERED_ACTIVE -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = false,
                canRecord = !contestHasntStarted,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = true,
                registerButtonText = "Đã đăng ký",
                recordButtonText = if (contestHasntStarted) "Cuộc thi chưa bắt đầu" else "Ghi nhận thành tích",
                registerButtonEnabled = false,
                recordButtonEnabled = !contestHasntStarted
            )

            UserContestStatus.REGISTERED_BLOCKED -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = false,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = true,
                registerButtonText = "Đã đăng ký",
                recordButtonText = ERegistrationStatus.BLOCK.value,
                registerButtonEnabled = false,
                recordButtonEnabled = false,
                statusMessage = "Tài khoản của bạn đã bị chặn khỏi cuộc thi này."
            )

            UserContestStatus.REGISTRATION_COMPLETED -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = false,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = true,
                registerButtonText = "Đã hoàn thành",
                recordButtonText = "Đã hoàn thành",
                registerButtonEnabled = false,
                recordButtonEnabled = false
            )

            UserContestStatus.CONTEST_EXPIRED -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = false,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = false,
                registerButtonText = contest.status?.value ?: "Đã kết thúc",
                recordButtonText = contest.status?.value ?: "Đã kết thúc",
                registerButtonEnabled = false,
                recordButtonEnabled = false
            )

            UserContestStatus.REGISTRATION_FULL -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = false,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = false,
                registerButtonText = "Số lượng đăng ký đã quá giới hạn",
                recordButtonText = "Ghi nhận thành tích",
                registerButtonEnabled = false,
                recordButtonEnabled = false
            )

            UserContestStatus.REGISTRATION_CLOSED -> ContestDisplayState(
                userStatus = userStatus,
                canRegister = false,
                canRecord = false,
                showLeaderboard = shouldShowLeaderboard(),
                showProgress = false,
                registerButtonText = "Hết hạn đăng ký",
                recordButtonText = "Ghi nhận thành tích",
                registerButtonEnabled = false,
                recordButtonEnabled = false
            )
        }
    }

    private fun shouldShowLeaderboard(): Boolean {
        return contest.isActive() || contest.isFinished()
    }

    fun getProgressInfo(): Triple<Double, Double, Int> {
        if (userRegistration == null) return Triple(0.0, 0.0, 0)

        val currentDistance = userRegistration.getTotalDistance()
        val contestDistance = contest.distance ?: 0.0
        val ratio = if (contestDistance > 0) (currentDistance / contestDistance) * 100 else 0.0

        return Triple(currentDistance, contestDistance, ratio.toInt())
    }

    fun canPerformAction(action: ContestAction): Boolean {
        val displayState = getDisplayState()
        return when (action) {
            ContestAction.REGISTER -> displayState.canRegister && displayState.registerButtonEnabled
            ContestAction.RECORD -> displayState.canRecord && displayState.recordButtonEnabled
            ContestAction.VIEW_LEADERBOARD -> displayState.showLeaderboard
            ContestAction.VIEW_PROGRESS -> displayState.showProgress
        }
    }

    enum class ContestAction {
        REGISTER,
        RECORD,
        VIEW_LEADERBOARD,
        VIEW_PROGRESS
    }
}