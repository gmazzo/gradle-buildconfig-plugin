package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Project

internal enum class PluginBindings(
    val pluginId: String,
    val handler: (Project, BuildConfigExtension) -> PluginBindingHandler<*>
) {

    JAVA("java", ::JavaHandler),

    KOTLIN_JVM("org.jetbrains.kotlin.jvm", ::KotlinHandler),

    KOTLIN_JS("org.jetbrains.kotlin.js", ::KotlinHandler),

    KOTLIN_LEGACY_JS("kotlin2js", ::KotlinHandler),

    KOTLIN_MULTIPLATFORM("org.jetbrains.kotlin.multiplatform", ::KotlinMultiplatformHandler);

}
