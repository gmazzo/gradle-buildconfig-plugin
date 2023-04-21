plugins {
    kotlin("js")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    js(IR) {
        nodejs()
    }
}

dependencies {
    testImplementation(libs.kotlin.test)
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("kotlin.IntArray", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
}
