package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigValue
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
                    val targetDependsOn = targetSpec.allDependsOn.filter { !it.isSuperseded }

                    val spec = (sequenceOf(targetSpec) + targetDependsOn)
                        .find { it.buildConfigFields.isNotEmpty() }
                        ?: return@all

                    val dependsOn = spec.allDependsOn
                        .filter { !it.isSuperseded }
                        .filter { it.hasExpects() }
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
                                    .filter { it.hasExpects() }
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

        private fun BuildConfigClassSpec.hasExpects() =
            buildConfigFields.any { it.value.orNull is BuildConfigValue.Expect }

        private fun BuildConfigClassSpec.hasActuals() =
            buildConfigFields.any { it.tags.get().contains(BuildConfigKotlinGenerator.TagActual) }

        private fun lookForExpectFields(spec: BuildConfigClassSpec, dependsOnSpecs: Set<BuildConfigClassSpec>) {
            val expectSpecs = linkedSetOf<BuildConfigClassSpec>()
            for (field in spec.buildConfigFields) {
                for (dependsOnSpec in dependsOnSpecs) {
                    val dependsOnField = dependsOnSpec.buildConfigFields.findByName(field.name) ?: continue

                    check(dependsOnField.value.orNull is BuildConfigValue.Expect) {
                        "Field '${dependsOnField.name}' in '$dependsOnSpec' must be `expect`, since it's defined as `actual` in '$spec'"
                    }

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

                    if (expectField.value.orNull.isExpectNoDefault) continue
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
                        if (expectField.value.orNull.isExpectNoDefault) continue

                        spec.buildConfigField(expectField)
                            .tags.add(BuildConfigKotlinGenerator.TagActual)

                        // also makes sure that the actual class matches the expect declaration
                        spec.defaultsFrom(expectSpec)
                    }
                }
            }
        }

        private val BuildConfigValue?.isExpectNoDefault: Boolean
            get() = this is BuildConfigValue.Expect && value == null

    }

}
