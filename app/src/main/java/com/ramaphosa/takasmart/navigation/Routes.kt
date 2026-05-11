package com.ramaphosa.takasmart.navigation

// Onboarding
const val ROUT_SPLASH       = "splash"
const val ROUT_ROLE_SELECT  = "role_select"
const val ROUT_PHONE_ENTRY  = "phone_entry"
const val ROUT_OTP_VERIFY = "otp_verify/{verificationId}"

// Household
const val ROUT_HOUSEHOLD_HOME   = "household_home"
const val ROUT_LOG_ITEM         = "log_item"
const val ROUT_SCHEDULE_PICKUP  = "schedule_pickup"
const val ROUT_REWARDS          = "rewards"
const val ROUT_TRACK_PICKUP     = "track_pickup/{pickupId}"

// Collector
const val ROUT_COLLECTOR_HOME   = "collector_home"
const val ROUT_EARNINGS         = "earnings"
const val ROUT_ACTIVE_PICKUP    = "active_pickup/{jobId}"
const val ROUT_WEIGH_ITEMS      = "weigh_items/{jobId}"

// Facility
const val ROUT_FACILITY_HOME    = "facility_home"
const val ROUT_VERIFY_DELIVERY  = "verify_delivery/{jobId}"
const val ROUT_CERTIFICATE      = "certificate/{jobId}"