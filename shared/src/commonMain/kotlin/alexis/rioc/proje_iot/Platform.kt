package alexis.rioc.proje_iot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform