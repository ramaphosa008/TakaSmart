package com.ramaphosa.takasmart.models

data class User(
    val uid           : String = "",
    val phone         : String = "",
    val role          : String = "household", // household | collector | facility
    val pointsBalance : Int    = 0,
    val recycledKg    : Double = 0.0,
    val pickupsDone   : Int    = 0
)