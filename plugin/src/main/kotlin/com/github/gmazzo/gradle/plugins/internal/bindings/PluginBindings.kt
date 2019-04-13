package com.github.gmazzo.gradle.plugins.internal.bindings

internal enum class PluginBindings(
    val pluginId: String,
    val handler: PluginBindingHandler
) {

    JAVA("java", JavaBindingHandler),

    KOTLIN_JVM("org.jetbrains.kotlin.jvm", KotlinJvmBindingHandler);

    //TODO KOTLIN_JS(KotlinJsExtensionHandler, "kotlin-platform-js", "kotlin2js"),

    //TODO KOTLIN_MULTIPLATFORM(KotlinMultiplatformExtensionHandler, "org.jetbrains.kotlin.multiplatform");

}
