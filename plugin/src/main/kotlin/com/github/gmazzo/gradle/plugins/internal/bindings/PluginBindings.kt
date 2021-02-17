package com.github.gmazzo.gradle.plugins.internal.bindings

internal enum class PluginBindings(
    val pluginId: String,
    val handler: PluginBindingHandler
) {

    JAVA("java", JavaBindingHandler),

    KOTLIN_JVM("org.jetbrains.kotlin.jvm", KotlinJvmBindingHandler),

    KOTLIN_JS("org.jetbrains.kotlin.js",  KotlinJsBindingHandler),

    KOTLIN_LEGACY_JS("kotlin2js", KotlinLegacyJsBindingHandler),

    KOTLIN_MULTIPLATFORM("org.jetbrains.kotlin.multiplatform", KotlinMultiplatformBindingHandler);

}
