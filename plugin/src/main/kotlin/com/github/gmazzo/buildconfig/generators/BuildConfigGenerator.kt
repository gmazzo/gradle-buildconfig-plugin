package com.github.gmazzo.buildconfig.generators

import org.gradle.api.Action
import org.gradle.api.tasks.Input

@JvmDefaultWithoutCompatibility
interface BuildConfigGenerator : Action<BuildConfigGeneratorSpec> {

    @get:Input
    val type: Class<out BuildConfigGenerator> get() = this::class.java

}
