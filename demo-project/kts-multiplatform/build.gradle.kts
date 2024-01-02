plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

kotlin {
    jvm()
    js(IR) { nodejs() }
}

dependencies {
    commonTestImplementation(libs.kotlin.test)
}

buildConfig {
    buildConfigField("COMMON_VALUE", "aCommonValue")

    sourceSets.named("jvmMain") {
        buildConfigField("PLATFORM", "jvm")
        buildConfigField( "JVM_VALUE", "aJvmValue")
    }

    sourceSets.named("jsMain") {
        buildConfigField( "PLATFORM", "js")
        buildConfigField( "JS_VALUE", "aJsValue")
    }
}

tasks.register("test") {
    dependsOn("allTests")
}

tasks.named("compileTestDevelopmentExecutableKotlinJs") {
    notCompatibleWithConfigurationCache("uses Task.project")
}
