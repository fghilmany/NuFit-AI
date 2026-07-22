package com.fghilmany.nufitai

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform