package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigField
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.github.gmazzo.buildconfig.internal.BuildConfigExtensionInternal
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_MAIN_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_TEST_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget.Companion.METADATA_TARGET_NAME

internal object KotlinBinder {

    fun Project.configure(extension: BuildConfigExtensionInternal, namer: (String) -> String = { it }) {
        kotlin.sourceSets.all { sourceSet ->
            val spec = extension.sourceSets.maybeCreate(namer(sourceSet.name))

            (sourceSet as ExtensionAware).registerExtension(spec)
        }

        afterEvaluate {
            kotlin.sourceSets.all { sourceSet ->
                val spec = extension.sourceSets.getByName(namer(sourceSet.name))

                sourceSet.kotlin.srcDir(spec)
                sourceSet.dependsOn
                    .map { extension.sourceSets.maybeCreate(namer(it.name)) }
                    .forEach(spec::dependsOn)
            }
        }
    }

    private val Project.kotlin: ExtensionAware /*KotlinProjectExtension*/
        get() = extensions.getByName<ExtensionAware>("kotlin")

    @Suppress("UNCHECKED_CAST")
    private val ExtensionAware/*KotlinProjectExtension*/.sourceSets
        get() = javaClass.getMethod("getSourceSets")
            .invoke(this) as NamedDomainObjectContainer<Named>

    private val Named/*KotlinSourceSet*/.kotlin
        get() = javaClass.getMethod("getKotlin")
            .invoke(this) as SourceDirectorySet

    private val Named/*KotlinSourceSet*/.dependsOn: Set<Named>
        get() {
            @Suppress("UNCHECKED_CAST")
            return javaClass.getMethod("getDependsOn")
                .invoke(this) as Set<Named>
        }

    object Multiplatform {

        @Suppress("UNCHECKED_CAST")
        private val ExtensionAware/*KotlinProjectExtension*/.targets
            get() = javaClass.getMethod("getTargets")
                .invoke(this) as NamedDomainObjectContainer<Named>

        @Suppress("UNCHECKED_CAST")
        private val Named/*KotlinTarget*/.compilations
            get() = javaClass.getMethod("getCompilations")
                .invoke(this) as NamedDomainObjectContainer<Named>

        @Suppress("UNCHECKED_CAST")
        private val Named/*KotlinCompilation*/.defaultSourceSet
            get() = javaClass.getMethod("getDefaultSourceSet")
                .invoke(this) as Named

        private fun nameOf(name: String): String = when (name) {
            COMMON_MAIN_SOURCE_SET_NAME -> MAIN_SOURCE_SET_NAME
            COMMON_TEST_SOURCE_SET_NAME -> TEST_SOURCE_SET_NAME
            else -> name
        }

        fun Project.configure(extension: BuildConfigExtensionInternal) {
            with(KotlinBinder) { configure(extension, ::nameOf) }

            afterEvaluate {
                computeExpectAndActuals(extension)
            }
        }

        private fun Project.computeExpectAndActuals(extension: BuildConfigExtensionInternal) {
            val specsOfTargets = linkedMapOf<BuildConfigClassSpec, Set<BuildConfigClassSpec>>()

            kotlin.targets.all { target ->
                if (target.name == METADATA_TARGET_NAME) return@all

                target.compilations.all { compilation ->
                    val ss = compilation.defaultSourceSet

                    val targetSpec = extension.sourceSets.getByName(nameOf(ss.name))

                    // adds a default `dependsOn` to either `main` or `test` source set (in case `applyDefaultHierarchyTemplate` was not used)
                    when (compilation.name) {
                        KotlinCompilation.MAIN_COMPILATION_NAME -> MAIN_SOURCE_SET_NAME
                        KotlinCompilation.TEST_COMPILATION_NAME -> TEST_SOURCE_SET_NAME
                        else -> null
                    }?.let { targetSpec.dependsOn(extension.sourceSets.getByName(it)) }

                    val targetDependsOn = targetSpec.allDependsOn.filter { !it.isSuperseded }

                    val spec = (sequenceOf(targetSpec) + targetDependsOn)
                        .filter { it.name != MAIN_SOURCE_SET_NAME && it.name != TEST_SOURCE_SET_NAME }
                        .find { it.buildConfigFields.isNotEmpty() }
                        ?: targetSpec

                    val dependsOn = spec.allDependsOn
                        .filter { !it.isSuperseded }
                        .toSet()

                    if (dependsOn.isNotEmpty()) {
                        lookForExpectFields(spec, dependsOn)
                        specsOfTargets[spec] = dependsOn
                    }

                    // find all the extra (traversing the depends on graph) with unique names
                    val extras = (sequenceOf(targetSpec) + targetDependsOn)
                        .flatMap { spec ->
                            val specDependsOn = spec.allDependsOn
                                .filter { !it.isSuperseded }

                            spec.extraSpecs.asSequence().map { extra ->
                                extra to specDependsOn
                                    .mapNotNull { it.extraSpecs.findByName(extra.name) }
                                    .toSet()
                            }
                        }
                        .distinctBy { (it, _) -> it.name }

                    for ((extra, extraDependsOn) in extras) {
                        if (extraDependsOn.isNotEmpty()) {
                            lookForExpectFields(extra, extraDependsOn)
                            specsOfTargets[extra] = extraDependsOn
                        }
                    }
                }
            }

            // finally, we make sure that all expects with defaults are present in a target (or any of its depends on)
            fillMissingActuals(specsOfTargets)
        }

        private fun lookForExpectFields(spec: BuildConfigClassSpec, dependsOnSpecs: Set<BuildConfigClassSpec>) {
            val expectSpecs = linkedSetOf<BuildConfigClassSpec>()
            for (field in spec.buildConfigFields) {
                for (dependsOnSpec in dependsOnSpecs) {
                    val dependsOnField = dependsOnSpec.buildConfigFields.findByName(field.name) ?: continue

                    dependsOnField.tags.add(BuildConfigKotlinGenerator.TagExpect)
                    field.tags.add(BuildConfigKotlinGenerator.TagActual)
                    expectSpecs.add(dependsOnSpec)

                    // also makes sure that the actual class matches the expect declaration
                    spec.defaultsFrom(dependsOnSpec)
                }
            }

            // then, in case we have mixed expect and regular constants in the same spec, we promote them all to this spec
            for (expectSpec in expectSpecs) {
                for (expectField in expectSpec.buildConfigFields) {
                    expectField.tags.add(BuildConfigKotlinGenerator.TagExpect)

                    if (spec.buildConfigFields.names.contains(expectField.name)) continue

                    spec.buildConfigField(expectField).tags.add(BuildConfigKotlinGenerator.TagActual)
                }
            }
        }

        private fun fillMissingActuals(specsOfTargets: Map<BuildConfigClassSpec, Set<BuildConfigClassSpec>>) {
            for ((spec, dependsOnSpecs) in specsOfTargets) {
                if (dependsOnSpecs.any { it.hasActuals() }) continue

                for (expectSpec in dependsOnSpecs) {
                    for (expectField in expectSpec.buildConfigFields) {
                        if (spec.buildConfigFields.names.contains(expectField.name)) continue

                        expectField.tags.add(BuildConfigKotlinGenerator.TagExpect)
                        spec.buildConfigField(expectField)
                            .tags.add(BuildConfigKotlinGenerator.TagActual)

                        // also makes sure that the actual class matches the expect declaration
                        spec.defaultsFrom(expectSpec)
                    }
                }
            }
        }

        private fun BuildConfigClassSpec.hasActuals() =
            buildConfigFields.any { it.isActual }

        private val BuildConfigField.isActual: Boolean
            get() = BuildConfigKotlinGenerator.TagActual in tags.get()

    }

}
