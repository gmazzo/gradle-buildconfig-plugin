package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import com.github.gmazzo.buildconfig.BuildConfigValue
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
                targets.all target@{ target ->
                    val targetName = target.name

                    if (targetName != KotlinMetadataTarget.METADATA_TARGET_NAME) {
                        target.compilations.all { compilation ->
                            val spec =
                                extension.sourceSets.maybeCreate(compilation.defaultSourceSet.name) as BuildConfigSourceSetInternal

                            val commonSpecs = compilation.allKotlinSourceSets.asSequence()
                                .flatMap { it.allDependsOn }
                                .map { it.name.regularSourceSetName }
                                .flatMap { it.includingCommonsForAndroid }
                                .filter { it != spec.name }
                                .mapNotNull { extension.sourceSets.findByName(it) as BuildConfigSourceSetInternal? }
                                .filter { it.buildConfigFields.any { field -> field.value.orNull is BuildConfigValue.MultiplatformExpect<*> } }
                                .toSet()

                            for (common in commonSpecs) {
                                fillActualFields(targetName, from = common, into = spec)
                                common.extraSpecs.all { fillActualFields(targetName, from = it, into = spec) }
                            }
                        }
                    }
                }
            }
        }

        private fun fillActualFields(targetName: String, from: BuildConfigClassSpec, into: BuildConfigSourceSetInternal) {
            val spec by lazy {
                into.forClass(packageName = from.packageName.orNull, className = from.className.get()) {
                    it.className.convention(from.className)
                    it.packageName.convention(from.packageName)
                    it.documentation.convention(from.documentation)
                }
            }

            from.buildConfigFields.all { commonField ->
                val commonValue = commonField.value.orNull as? BuildConfigValue.MultiplatformExpect<*> ?: return@all
                val targetValue = commonValue.producer.resolveValue(forTarget = targetName)

                spec.buildConfigField(commonField.name) { actualField ->
                    actualField.type.value(commonField.type).disallowChanges()
                    actualField.value.value(BuildConfigValue.MultiplatformActual(targetValue)).disallowChanges()
                    actualField.position.value(commonField.position).disallowChanges()
                }
            }
        }

        // Named stands for KotlinSourceSet here
        private val Named.allDependsOn: Sequence<Named>
            get() = sequenceOf(this) + dependsOn.flatMap { it.allDependsOn }

        private val String.includingCommonsForAndroid: Sequence<String>
            get() = when (this) {
                "androidMain" -> sequenceOf(this, SourceSet.MAIN_SOURCE_SET_NAME)
                "androidUnitTest" -> sequenceOf(this, SourceSet.TEST_SOURCE_SET_NAME)
                else -> sequenceOf(this)
            }

        private val String.regularSourceSetName: String
            get() = when (this) {
                KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME -> SourceSet.MAIN_SOURCE_SET_NAME
                KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME -> SourceSet.TEST_SOURCE_SET_NAME
                else -> this
            }

    }

}
