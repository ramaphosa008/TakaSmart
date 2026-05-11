package com.ramaphosa.takasmart.ui.screens.shared

data class DummyItem(
    val id       : String,
    val category : String,
    val model    : String,
    val status   : String,
    val photoUrl : String
)

data class DummyPickup(
    val id          : String,
    val status      : String,
    val scheduledAt : String,
    val itemCount   : Int
)