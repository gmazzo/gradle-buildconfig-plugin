@file:Suppress("OPT_IN_USAGE")

import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
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
    namespace = "com.github.gmazzo.buildconfig.demos.android"
    buildFeatures.buildConfig = true
    defaultConfig.minSdk = 21

    flavorDimensions += listOf("brand", "env")
    productFlavors {
        create("stage") { dimension = "env" }
        create("prod") { dimension = "env" }
        create("bar") { dimension = "brand" }
        create("foo") { dimension = "brand" }
    }
}

dependencies {
    commonMainImplementation(libs.uriKMP)
    commonTestImplementation(libs.kotlin.test)
}

buildConfig {
    buildConfigField("COMMON_VALUE",  "aCommonValue")
    buildConfigField("IS_MOBILE", expect(false)) // with a default
    buildConfigField("PLATFORM", expect<String>()) // without a default
    buildConfigField("DEBUG", expect(false)) // to be changed by an Android variant
    buildConfigField("com.eygraber.uri.Uri", "ENDPOINT", expect(expression("Uri.parse(\"https://api.example.com\")")))
    buildConfigField("PRODUCT_VALUE", expect<String?>(null))

    forClass("i18n") {
        useKotlinOutput { topLevelConstants = true }

        buildConfigField("i18n_hello", "Hello")
        buildConfigField("i18n_kind", expect<String>())
    }

    sourceSets.named("androidMain") {
        buildConfigField("PLATFORM", "android")
        buildConfigField("IS_MOBILE", true)
        buildConfigField("ANDROID_VALUE", "anAndroidValue")
        forClass("i18n").buildConfigField("i18n_kind", "android")
    }

    sourceSets.named("androidDebug") {
        buildConfigField("DEBUG", true)
    }

    sourceSets.named("androidStage") {
        buildConfigField("com.eygraber.uri.Uri", "ENDPOINT", expression("Uri.parse(\"https://stage.api.example.com\")"))
    }

    sourceSets.named("androidFoo") {
        buildConfigField<String?>("PRODUCT_VALUE", "fooValue")
    }

    sourceSets.named("androidBar") {
        buildConfigField<String?>("PRODUCT_VALUE", "barValue")
    }

    sourceSets.named("jvmMain") {
        buildConfigField("PLATFORM", "jvm")
        buildConfigField("JVM_VALUE", "aJvmValue")
        forClass("i18n").buildConfigField("i18n_kind", "jvm")
    }

    sourceSets.named("iosMain") {
        buildConfigField("PLATFORM", "ios")
        buildConfigField("IS_MOBILE", true)
        buildConfigField("IOS_VALUE", "anIOSValue")
        forClass("i18n").buildConfigField("i18n_kind", "ios")
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

        forClass("i18n").buildConfigField("i18n_kind", "web")
    }

    sourceSets.named("jsMain") {
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
