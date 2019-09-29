package com.github.gmazzo.gradle.plugins.internal.bindings

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal object KotlinJvmBindingHandler : KotlinBindingHandler() {

    override val KotlinSourceSet.compileTaskName: String
        get() = "compile${name.taskPrefix}Kotlin"

}
