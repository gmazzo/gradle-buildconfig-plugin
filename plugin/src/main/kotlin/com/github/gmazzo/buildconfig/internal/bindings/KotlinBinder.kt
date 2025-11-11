package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.BuildConfigValue
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

        // KotlinSourceSet.dependsOn
        private val Named.dependsOn: Set<Named>
            get() {
                @Suppress("UNCHECKED_CAST")
                return javaClass.getMethod("getDependsOn")
                    .invoke(this) as Set<Named>
            }

        fun Project.configure(extension: BuildConfigExtension) {
            configure(extension) { it.name.regularSourceSetName }

            afterEvaluate {
                kotlinSourceSets.all { ss ->
                    val spec =
                        extension.sourceSets.getByName(ss.name.regularSourceSetName) as BuildConfigSourceSetInternal
                    val dependsOnSpecs = ss.allDependsOn
                        .map { it.name.regularSourceSetName }
                        .let { it + ss.name.includingCommonsForAndroid }
                        .toSet()
                        .mapNotNull { extension.sourceSets.findByName(it) as BuildConfigSourceSetInternal? }

                    lookForExpectFields(spec, dependsOnSpecs)
                    spec.extraSpecs.all { extra ->
                        lookForExpectFields(extra, dependsOnSpecs) { it.extraSpecs.findByName(extra.name) }
                    }
                }
            }
        }

        private fun lookForExpectFields(
            spec: BuildConfigClassSpec,
            dependsOnSpecs: List<BuildConfigSourceSetInternal>,
            forExtra: ((BuildConfigSourceSetInternal) -> BuildConfigClassSpec?)? = null,
        ) {
            val target = forExtra?.invoke(spec as BuildConfigSourceSetInternal) ?: spec

            val expectSpecs = mutableSetOf<BuildConfigClassSpec>()
            for (field in spec.buildConfigFields) {
                val (expectSpec, expectField) = dependsOnSpecs.asSequence()
                    .mapNotNull { if (forExtra != null) forExtra(it) else it }
                    .mapNotNull { it.buildConfigFields.findByName(field.name)?.let { ef -> it to ef } }
                    .find { (_, it) -> it.value.orNull is BuildConfigValue.Expect }
                    ?: continue

                expectField.tags.add(BuildConfigKotlinGenerator.TagExpect)
                field.tags.add(BuildConfigKotlinGenerator.TagActual)

                // also makes sure that the actual class matches the expect declaration
                target.className.convention(expectSpec.className)
                target.packageName.convention(expectSpec.packageName)
                target.documentation.convention(expectSpec.documentation)

                expectSpecs.add(expectSpec)
            }

            // finally, in case we have mixed expect and regular constants in the same spec, we promote them all to this spec
            for (expectSpec in expectSpecs) {
                for (expectField in expectSpec.buildConfigFields) {
                    val expectDefault = when (val value = expectField.value.orNull) {
                        is BuildConfigValue.Expect -> when (val defaultValue = value.value) {
                            is BuildConfigValue.NoDefault -> continue
                            else -> BuildConfigValue.Literal(defaultValue)
                        }
                        else -> value
                    }

                    expectField.tags.add(BuildConfigKotlinGenerator.TagExpect)

                    if (target.buildConfigFields.names.contains(expectField.name)) continue

                    target.buildConfigFields.create(expectField.name) { field ->
                        field.type.value(expectField.type).disallowChanges()
                        field.value.value(expectDefault).disallowChanges()
                        field.position.value(expectField.position).disallowChanges()
                        field.tags.add(BuildConfigKotlinGenerator.TagActual)
                    }
                }
            }
        }

        // Named stands for KotlinSourceSet here
        private val Named.allDependsOn: Sequence<Named>
            get() = dependsOn.asSequence().flatMap { sequenceOf(it) + it.allDependsOn }

        private val String.includingCommonsForAndroid: Sequence<String>
            get() = when (this) {
                "androidMain" -> sequenceOf(SourceSet.MAIN_SOURCE_SET_NAME)
                "androidUnitTest" -> sequenceOf(SourceSet.TEST_SOURCE_SET_NAME)
                else -> emptySequence()
            }

        private val String.regularSourceSetName: String
            get() = when (this) {
                KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> SourceSet.MAIN_SOURCE_SET_NAME
                KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME -> SourceSet.TEST_SOURCE_SET_NAME
                else -> this
            }

    }

}
