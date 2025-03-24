package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal object KotlinBinder {

    fun Project.configure(extension: BuildConfigExtension) =
        configure(extension) { it.name }

    private fun Project.configure(extension: BuildConfigExtension, nameOf: (Named) -> String = { it.name }) {
        kotlinSourceSets.all { sourceSet ->
            val spec = extension.sourceSets.maybeCreate(nameOf(sourceSet))

            (sourceSet as ExtensionAware).registerExtension(spec)
            sourceSet.kotlinSrcDir(spec)
        }

        extension.generator.convention(BuildConfigKotlinGenerator())
    }

    // project.kotlin.sourceSets
    private val Project.kotlinSourceSets
        get() = with(extensions.getByName("kotlin")) {
            @Suppress("UNCHECKED_CAST")
            javaClass.getMethod("getSourceSets")
                .invoke(this) as NamedDomainObjectContainer<Named>
        }

    // KotlinSourceSet.kotlin.srcDir(spec)
    private fun Named.kotlinSrcDir(spec: BuildConfigSourceSet) {
        (javaClass.getMethod("getKotlin")
            .invoke(this) as SourceDirectorySet)
            .srcDir(spec)
    }

    object Multiplatform {

        fun Project.configure(extension: BuildConfigExtension) = configure(extension) {
            when (val name = it.name) {
                KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> SourceSet.MAIN_SOURCE_SET_NAME
                KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME -> SourceSet.TEST_SOURCE_SET_NAME
                else -> name
            }
        }

    }

}
