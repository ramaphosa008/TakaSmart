package com.ramaphosa.takasmart.models

data class CloudinaryResponse(

    val url: String,        //Non-Secure url - one that starts with https
    val secure_url:String,
    val public_id: String

)