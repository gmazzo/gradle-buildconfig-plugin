package com.github.gmazzo.buildconfig.generators

import org.gradle.api.Action
import org.gradle.api.tasks.Input

public interface BuildConfigGenerator : Action<BuildConfigGeneratorSpec> {

    @get:Input
    public val type: Class<out BuildConfigGenerator> get() = this::class.java

}
