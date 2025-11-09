package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.setProperty
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

internal object KotlinBinder {

    fun Project.configure(extension: BuildConfigExtension) =
        configure(extension) { it.name }

    private fun Project.configure(extension: BuildConfigExtension, nameOf: (Named) -> String = { it.name }) {
        kotlinSourceSets.all { sourceSet ->
            val spec = extension.sourceSets.maybeCreate(nameOf(sourceSet))

            (sourceSet as ExtensionAware).registerExtension(spec)
        }

        // Kotlin does some source set sanity checks which will cause an eager instantiation of the generate task
        // while in the configuration phase.
        // This will finalize DSL properties before the build script have a change to change them
        afterEvaluate {
            kotlinSourceSets.all { sourceSet ->
                sourceSet.kotlinSrcDir(extension.sourceSets.getByName(nameOf(sourceSet)))
            }
        }
    }

    // project.kotlin
    private val Project.kotlin: ExtensionAware
        get() = extensions.getByName<ExtensionAware>("kotlin")

    // project.kotlin.sourceSets
    private val Project.kotlinSourceSets
        get() = with(kotlin) {
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

        // project.kotlin.targets
        private val Project.targets
            get() = with(kotlin) {
                @Suppress("UNCHECKED_CAST")
                javaClass.getMethod("getTargets")
                    .invoke(this) as NamedDomainObjectContainer<Named>
            }

        // KotlinTarget.compilations
        private val Named.compilations: NamedDomainObjectContainer<Named>
            get() {
                @Suppress("UNCHECKED_CAST")
                return javaClass.getMethod("getCompilations")
                    .invoke(this) as NamedDomainObjectContainer<Named>
            }

        // KotlinCompilation.allKotlinSourceSets
        private val Named.allKotlinSourceSets: Set<Named>
            get() {
                @Suppress("UNCHECKED_CAST")
                return javaClass.getMethod("getAllKotlinSourceSets")
                    .invoke(this) as Set<Named>
            }

        // KotlinCompilation.defaultSourceSet
        private val Named.defaultSourceSet: Named
            get() {
                @Suppress("UNCHECKED_CAST")
                return javaClass.getMethod("getDefaultSourceSet")
                    .invoke(this) as Named
            }

        fun Project.configure(extension: BuildConfigExtension) {
            configure(extension) {
                when (val name = it.name) {
                    KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> SourceSet.MAIN_SOURCE_SET_NAME
                    KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME -> SourceSet.TEST_SOURCE_SET_NAME
                    else -> name
                }
            }

            targets.all target@{ target ->
                val targetName = target.name

                if (targetName != KotlinMetadataTarget.METADATA_TARGET_NAME) {
                    target.compilations.all { compilation ->
                        val spec =
                            extension.sourceSets.maybeCreate(compilation.defaultSourceSet.name) as BuildConfigSourceSetInternal

                        val commonSpecs =
                            compilation.allKotlinSourceSets.asSequence()
                                .map { it.name }
                                .filter { it != spec.name }
                                .mapNotNull { extension.sourceSets.findByName(it) as BuildConfigSourceSetInternal? }
                                .toList()

                        spec.fillActualFields(objects, commonSpecs)
                        spec.extraSpecs.all {
                            spec.fillActualFields(objects, commonSpecs, forExtra = it)
                        }
                    }
                }
            }
        }

        private fun BuildConfigSourceSetInternal.fillActualFields(
            objects: ObjectFactory,
            commonSourceSets: List<BuildConfigSourceSetInternal>,
            forExtra: BuildConfigClassSpec? = null,
        ) {

            val commonSpec = when (forExtra) {
                null -> commonSourceSets
                else -> commonSourceSets.mapNotNull { it.extraSpecs.findByName(forExtra.name) }
            }
            val commonFields = objects.setProperty<String>()
            for (common in commonSpec) {
                common.buildConfigFields.all {
                    commonFields.add(it.name)
                }
            }
            commonFields.finalizeValueOnRead()

            buildConfigFields.all { field ->
                field.tags.addAll(commonFields.map { commons ->
                    if (field.name in commons) listOf(BuildConfigKotlinGenerator.ActualField)
                    else emptyList()
                })
            }
        }

    }

}
