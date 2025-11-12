package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer

internal object JavaBinder {

    fun Project.configure(extension: BuildConfigExtension) {
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)

        sourceSets.configureEach { sourceSet ->
            val spec = extension.sourceSets.maybeCreate(sourceSet.name)

            sourceSet.registerExtension(spec)
            sourceSet.java.srcDir(spec)
        }
    }

    internal fun ExtensionAware.registerExtension(sourceSet: BuildConfigSourceSet) =
        extensions.add(BuildConfigSourceSet::class.java, "buildConfig", sourceSet)

}
