package uk.co.androidmcr.memorymatch

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform