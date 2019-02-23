import java.util.*

plugins {
    id("com.github.gmazzo.buildconfig") version "<local>"
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")

    // delegated field example
    buildConfigField("int", "MAGIC_CODE") {
        "0x" + Random().nextInt(1000).toString(16)
    }
}
