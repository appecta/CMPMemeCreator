package com.eag.tflmeme

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform