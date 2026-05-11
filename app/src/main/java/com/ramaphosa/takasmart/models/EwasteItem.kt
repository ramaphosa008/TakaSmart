package com.ramaphosa.takasmart.models

data class EwasteItem(
    val id        : String = "",
    val userId    : String = "",
    val category  : String = "",
    val condition : String = "",
    val model     : String = "",
    val photoUrl  : String = "", // Cloudinary URL stored here
    val status    : String = "pending" // pending | collected | recycled
)