plugins {
    kotlin("js")
    id("com.github.gmazzo.buildconfig") version "<latest>"
}

kotlin {
    js {
        nodejs()
    }
}

dependencies {
    "implementation"(kotlin("stdlib-js"))
    "testImplementation"(kotlin("test-js"))
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("kotlin.IntArray", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
}
