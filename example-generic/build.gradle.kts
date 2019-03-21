plugins {
    id("com.github.gmazzo.buildconfig") version "<local>"
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")

    buildConfig.forClass("BuildResources") {
        buildConfigField("String", "A_CONSTANT", "\"aConstant\"")
    }
}

val generateBuildConfig by tasks
val generateBuildResourcesBuildConfig by tasks

task("test") {
    dependsOn(generateBuildConfig, generateBuildResourcesBuildConfig)
}
