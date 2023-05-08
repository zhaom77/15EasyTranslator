rootProject.extra.apply {
    set("androidPlugin", "com.android.tools.build:gradle:7.2.1")
    set("kotlinVersion", "1.8.20")
}

repositories {
    google()
    jcenter()
    maven("https://jitpack.io")
    mavenCentral()
}
