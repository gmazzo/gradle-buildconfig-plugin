package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal object KotlinJsBindingHandler : KotlinBindingHandler() {

    override val KotlinSourceSet.compileTaskName: String
        get() = "compile${name.taskPrefix}KotlinJs"

}
