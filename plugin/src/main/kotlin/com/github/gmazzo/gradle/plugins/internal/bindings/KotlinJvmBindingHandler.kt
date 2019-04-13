package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.internal.bindings.JavaBindingHandler.javaSourceSets
import org.gradle.api.Project
import org.gradle.api.internal.plugins.DslObject
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal object KotlinJvmBindingHandler : KotlinBindingHandler() {

    override fun Project.discoverSourceSets(onSourceSet: (KotlinSourceSet) -> Unit) {
        javaSourceSets.all { ss ->
            DslObject(ss).convention.getPlugin(KotlinSourceSet::class.java).let(onSourceSet)
        }
    }

}