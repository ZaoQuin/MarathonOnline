package com.university.marathononline.utils

import android.content.Intent

const val ACTIVITY_RECOGNITION_REQUEST_CODE = 100

const val NO_BACK_STACK_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
const val EMAIL="marathononlineute@gmail.com"
const val SENDER_PASS="jlhtwexvmcmmpart"
const val KEY_ALIAS = "MyAppKeyAlias"

const val KEY_USER = "user"
const val KEY_CONTEST = "contest"
const val KEY_UPDATE_CONTEST = "update-contest"
const val KEY_CONTESTS = "list-contest"
const val KEY_NOTIFICATIONS = "list-notifications"
const val KEY_NOTIFICATION_DATA = "list-notifications-data"
const val KEY_RECORD = "record"
const val KEY_RECORDS = "list-record"
const val KEY_REWARD = "reward"
const val KEY_REWARDS = "list-rewards"
const val KEY_REGISTRATIONS = "list-registrations"
const val KEY_TOTAL_DISTANCE = "total-distance"
const val KEY_TOTAL_TIME = "total-time"
const val KEY_TOTAL_STEPS = "total-steps"
const val KEY_AVG_SPEED = "avgSpeed"
const val KEY_CALORIES = "calories"
const val KEY_PACE = "pace"


const val KEY_TRAINING_PLAN = "training_plan"
const val KEY_TRAINING_PLAN_ID = "training_plan_id"
const val KEY_TRAINING_DAY = "training_day"

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


const val SORT_BY_ASC = "SORT_BY_ASC"
const val SORT_BY_DES = "SORT_BY_DES"

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