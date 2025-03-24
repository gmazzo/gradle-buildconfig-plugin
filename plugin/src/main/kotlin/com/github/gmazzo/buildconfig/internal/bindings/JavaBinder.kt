package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.generators.BuildConfigJavaGenerator
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

internal object JavaBinder {

    fun Project.configure(extension: BuildConfigExtension) {
        val sourceSets: SourceSetContainer by extensions

        sourceSets.configureEach { sourceSet ->
            val spec = extension.sourceSets.maybeCreate(sourceSet.name)

            sourceSet.registerExtension(spec)
            sourceSet.java.srcDir(spec)
        }

        extension.generator.convention(BuildConfigJavaGenerator())
    }

    internal fun ExtensionAware.registerExtension(sourceSet: BuildConfigSourceSet) =
        extensions.add(BuildConfigSourceSet::class, "buildConfig", sourceSet)

}
