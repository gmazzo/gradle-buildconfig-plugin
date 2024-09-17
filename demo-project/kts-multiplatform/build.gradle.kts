import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

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
    generator = object : BuildConfigKotlinGenerator() {
        override fun adaptSpec(spec: TypeSpec) = spec.toBuilder()
            .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("kotlin.js.JsName"))
                .addMember("name = %S", spec.name!!)
                .build())
            .build()
    }

    buildConfigField("COMMON_VALUE", "aCommonValue")

    sourceSets.named("jvmMain") {
        useKotlinOutput() // resets `generator` back to default's Kotlin generator for JVM
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
