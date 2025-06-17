package com.university.marathononline.utils

import android.content.Intent

const val ACTIVITY_RECOGNITION_REQUEST_CODE = 100

const val NO_BACK_STACK_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
const val EMAIL="marathononlineute@gmail.com"
const val SENDER_PASS="jlhtwexvmcmmpart"
const val KEY_ALIAS = "MyAppKeyAlias"

const val KEY_USER = "user"
const val KEY_CONTEST = "contest"
const val KEY_CONTEST_ID = "contest-id"
const val KEY_UPDATE_CONTEST = "update-contest"
const val KEY_CONTESTS = "list-contest"
const val KEY_NOTIFICATION = "notification"
const val KEY_NOTIFICATION_ID = "notification-id"
const val KEY_NOTIFICATION_TYPE = "notification-type"
const val KEY_NOTIFICATIONS = "list-notifications"
const val KEY_NOTIFICATION_DATA = "notifications-data"
const val KEY_RECORD = "record"
const val KEY_RECORDS = "list-record"
const val KEY_REWARD = "reward"
const val KEY_REWARDS = "list-rewards"
const val KEY_REWARD_ID = "reward-id"
const val KEY_OBJECT_ID = "object-id"

const val KEY_REGISTRATIONS = "list-registrations"
const val KEY_REGISTRATION_ID = "registration-id"
const val KEY_TOTAL_DISTANCE = "total-distance"
const val KEY_TOTAL_TIME = "total-time"
const val KEY_TOTAL_STEPS = "total-steps"
const val KEY_AVG_SPEED = "avgSpeed"
const val KEY_CALORIES = "calories"
const val KEY_PACE = "pace"

const val KEY_TRAINING_PLAN = "training_plan"
const val KEY_TRAINING_PLAN_ID = "training_plan_id"
const val KEY_TRAINING_DAY = "training_day"
const val KEY_FEEDBACK = "feedback"
const val KEY_FEEDBACK_ID = "feedback-id"
const val KEY_FEEDBACK_TYPE = "feedback-type"
const val KEY_RECORD_ID = "record-id"

const val KEY_ROLE = "role"
const val KEY_FULL_NAME = "fullName"
const val KEY_EMAIL = "email"
const val KEY_PASSWORD = "password"
const val KEY_AUTH_TOKEN = "auth_token"
const val KEY_AUTH_ROLE = "auth_role"
const val KEY_AUTH_STATUS = "auth_status"
const val KEY_AUTH_DELETED = "deleted"
const val KEY_PASSWORD_IV = "password_iv"
const val KEY_REMEMBER_ME = "remember_me"
const val LAST_SYNC_TIME = "last_sync_time"

const val KEY_MESSAGE = "message"

const val SORT_BY_ASC = "SORT_BY_ASC"
const val SORT_BY_DES = "SORT_BY_DES"

const val OPEN_FEEDBACK_TAB = "open-feedback-tab"

const val ADMIN_FEEDBACK = "ADMIN_FEEDBACK"
const val RUNNER_FEEDBACK = "RUNNER_FEEDBACK"

const val ACTION_NEW_NOTIFICATION = "com.university.marathononline.NEW_NOTIFICATION"
const val ACTION_NEW_FEEDBACK = "com.university.marathononline.NEW_FEEDBACK"
const val ACTION_UPDATE_BADGE = "com.university.marathononline.UPDATE_BADGE"
const val SHOW_FEEDBACK_DIALOG = "com.university.marathononline.SHOW_FEEDBACK_DIALOG"
const val FEEDBACK_UPDATED = "com.university.marathononline.FEEDBACK_UPDATED"
const val NOTIFICATION_READ = "com.university.marathononline.NOTIFICATION_READ"

object WearableConstants {
    const val DATA_PATH = "/wear_health_data"
    const val START_RECORDING_PATH = "/start_recording"
    const val STOP_RECORDING_PATH = "/stop_recording"

    const val KEY_HEART_RATE = "heartRate"
    const val KEY_STEPS = "steps"
    const val KEY_DISTANCE = "distance"
    const val KEY_SPEED = "speed"
    const val KEY_CALORIES = "calories"
    const val KEY_TIMESTAMP = "timestamp"
    const val KEY_IS_RECORDING = "isRecording"
}

object FgRecordConstants {
    const val CHANNEL_ID = "RunningServiceChannel"
    const val NOTIFICATION_ID = 1
    const val ACTION_STOP = "ACTION_STOP"
    const val ACTION_PLAY = "ACTION_PLAY"
    const val ACTION_PAUSE = "ACTION_PAUSE"

    const val RUNNING_UPDATE = "RUNNING_UPDATE"
    const val RUNNING_STOPPED = "RUNNING_STOPPED"
    const val time = "time"
    const val distance = "distance"
    const val pace = "pace"
    const val isRecording = "isRecording"
    const val isPaused = "isPaused"
    const val isStopping = "isStopping"
    const val steps = "steps"
    const val avgSpeed = "avgSpeed"
    const val startTime = "startTime"
    const val endTime = "endTime"
    const val isSavingRecord = "isSavingRecord"
}