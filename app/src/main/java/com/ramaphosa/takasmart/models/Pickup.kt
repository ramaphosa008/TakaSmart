package com.ramaphosa.takasmart.models

data class Pickup(
    val id                 : String = "",
    val householdId        : String = "",
    val collectorId        : String = "",
    val facilityId         : String = "",
    val address            : String = "",
    val scheduledAt        : String = "",
    val itemIds            : List<String> = emptyList(),
    val status             : String = "requested",
    // requested | confirmed | en_route | at_household | at_facility | completed | disputed
    val otp                : String = "",
    val collectorLoggedKg  : Double = 0.0,
    val facilityVerifiedKg : Double = 0.0,
    val verifiedKg         : Double = 0.0
)