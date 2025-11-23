@file:Suppress("OPT_IN_USAGE")

import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

plugins {
    alias(libs.plugins.android.multiplatform)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.ksp)
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

kotlin {
    androidLibrary { // new AGP KMP plugin does not supports variants nor app
        compileSdk = 36
        namespace = "com.github.gmazzo.buildconfig.demos.android"
        minSdk = 21

        withHostTest { } // the old `androidUnitTest` variant
        withDeviceTest { } // the old `androidInstrumentedTest` variant
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    js(IR) { nodejs() }
    wasmJs { nodejs() }
    applyDefaultHierarchyTemplate()
}

dependencies {
    "kspJvm"(libs.autoservice.ksp)
    "jvmMainCompileOnly"(libs.autoservice)
    commonMainImplementation(libs.uriKMP)
    commonTestImplementation(libs.kotlin.test)
}

buildConfig {
    buildConfigField("COMMON_VALUE",  expect("aCommonValue"))   // a constant for all platforms
    buildConfigField("PLATFORM", expect<String>())                          // expect a platform specific value
    buildConfigField("DEBUG", expect(false))                    // expect with a default
    buildConfigField("com.eygraber.uri.Uri", "ENDPOINT",
        expect(expression("Uri.parse(\"https://api.example.com\")")))
    buildConfigField("PRODUCT_VALUE", expect<String?>(null))

    forClass("i18n") {
        useKotlinOutput { topLevelConstants = true }

        buildConfigField("i18n_hello", expect("Hello"))
        buildConfigField("i18n_kind", expect<String>())
    }

    forClass("Single") {
        buildConfigField("IS_MOBILE", expect(false))
    }

    sourceSets.named("androidMain") {
        buildConfigField("PLATFORM", "android")
        buildConfigField("ANDROID_VALUE", "anAndroidValue")
        buildConfigField("DEBUG", false)
        forClass("i18n").buildConfigField("i18n_kind", "android")
        forClass("Single").buildConfigField("IS_MOBILE", true)
    }

    sourceSets.named("test") {
        buildConfigField("TEST_VALUE", "aTestValue")
    }

    sourceSets.named("jvmMain") {
        buildConfigField("PLATFORM", "jvm")
        buildConfigField("JVM_VALUE", "aJvmValue")
        forClass("i18n").buildConfigField("i18n_kind", "jvm")
        forClass("Single") // will inherit all defaults from `commonMain`
    }

    sourceSets.named("iosMain") {
        buildConfigField("PLATFORM", "ios")
        buildConfigField("IOS_VALUE", "anIOSValue")
        forClass("i18n").buildConfigField("i18n_kind", "ios")
        forClass("Single").buildConfigField("IS_MOBILE", true)
    }

    sourceSets.named("webMain") {
        // Customize the generator to add @JsName annotations to the generated object
        generator = object : BuildConfigKotlinGenerator() {
            override fun adaptSpec(spec: TypeSpec) = spec.toBuilder()
                .addAnnotation(
                    AnnotationSpec.builder(ClassName.bestGuess("kotlin.js.JsName"))
                        .addMember("name = %S", spec.name!!)
                        .build()
                )
                .build()
        }

        buildConfigField("PLATFORM", "web")
        buildConfigField("WEB_VALUE", "aWebValue")
        forClass("i18n").buildConfigField("i18n_kind", "web")
        forClass("Single") // will inherit all defaults from `commonMain`
    }
}

tasks.named("compileTestDevelopmentExecutableKotlinJs") {
    notCompatibleWithConfigurationCache("uses Task.project")
}
