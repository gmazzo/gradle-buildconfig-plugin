@file:Suppress("OPT_IN_USAGE")

import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android)
    id("com.github.gmazzo.buildconfig")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

kotlin {
    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()
    js(IR) { nodejs() }
    wasmJs { nodejs() }
    applyDefaultHierarchyTemplate()
}

android {
    compileSdk = 36
    namespace = "com.github.gmazzo.buildconfig.demos.kts_multiplatform"
}

dependencies {
    commonTestImplementation(libs.kotlin.test)
}

buildConfig {
    buildConfigField("COMMON_VALUE",  "aCommonValue")
    buildConfigField("IS_MOBILE", expect(false)) // with a default
    buildConfigField("PLATFORM", expect<String>()) // without a default
    buildConfigField("DEBUG", expect(false)) // to be changed by an Android variant

    sourceSets.named("androidMain") {
        buildConfigField("PLATFORM", "android")
        buildConfigField("IS_MOBILE", true)
        buildConfigField("ANDROID_VALUE", "anAndroidValue")
    }

    sourceSets.named("androidDebug") {
        buildConfigField("DEBUG", true)
    }

    sourceSets.named("jvmMain") {
        buildConfigField("PLATFORM", "jvm")
        buildConfigField("JVM_VALUE", "aJvmValue")
    }

    sourceSets.named("iosMain") {
        buildConfigField("PLATFORM", "ios")
        buildConfigField("IS_MOBILE", true)
        buildConfigField("IOS_VALUE", "anIOSValue")
    }

    sourceSets.named("jsMain") {
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

        buildConfigField("PLATFORM", "js")
        buildConfigField("JS_VALUE", "aJsValue")
    }

    sourceSets.named("wasmJsMain") {
        buildConfigField("PLATFORM", "wasmJs")
        buildConfigField("WASM_JS_VALUE", "aWebAssemblyJsValue")
    }
}

tasks.named("compileTestDevelopmentExecutableKotlinJs") {
    notCompatibleWithConfigurationCache("uses Task.project")
}
