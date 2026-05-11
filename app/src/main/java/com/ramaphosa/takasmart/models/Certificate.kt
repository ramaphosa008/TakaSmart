package com.ramaphosa.takasmart.models

data class Certificate(
    val id          : String = "",
    val pickupId    : String = "",
    val householdId : String = "",
    val collectorId : String = "",
    val facilityId  : String = "",
    val kgProcessed : Double = 0.0,
    val certUrl     : String = "" // PDF URL if generated
)