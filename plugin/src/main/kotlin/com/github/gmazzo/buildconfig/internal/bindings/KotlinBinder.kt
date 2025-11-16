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
                kotlin.targets.all { target ->
                    target.compilations.all { compilation ->
                        val ss = compilation.defaultSourceSet
                        val spec = extension.sourceSets.getByName(nameOf(ss.name))
                        val dependsOnSpecs = spec.allDependsOn.filter { !it.isSuperseded }.toSet()

                        lookForExpectFields(spec, dependsOnSpecs)
                        spec.extraSpecs.all { extra ->
                            val extraDependsOn = dependsOnSpecs
                                .mapNotNull { it.extraSpecs.findByName(extra.name) }
                                .toSet()

                            lookForExpectFields(extra, extraDependsOn)
                        }
                    }
                }
            }
        }

        private fun lookForExpectFields(spec: BuildConfigClassSpec, dependsOnSpecs: Set<BuildConfigClassSpec>) {
            val expectSpecs = linkedSetOf<BuildConfigClassSpec>()
            for (field in spec.buildConfigFields) {
                for (dependsOnSpec in dependsOnSpecs) {
                    val dependsOnField = dependsOnSpec.buildConfigFields.findByName(field.name) ?: continue
                    if (dependsOnField.value.orNull !is BuildConfigValue.Expect) continue

                    dependsOnField.tags.add(BuildConfigKotlinGenerator.TagExpect)
                    field.tags.add(BuildConfigKotlinGenerator.TagActual)
                    expectSpecs.add(dependsOnSpec)

                    // also makes sure that the actual class matches the expect declaration
                    spec.defaultsFrom(dependsOnSpec)
                }
            }

            // finally, in case we have mixed expect and regular constants in the same spec, we promote them all to this spec
            for (expectSpec in expectSpecs) {
                for (expectField in expectSpec.buildConfigFields) {
                    val expectDefault = when (val value = expectField.value.orNull) {
                        is BuildConfigValue.Expect -> when (val defaultValue = value.value) {
                            is BuildConfigValue.NoDefault -> continue
                            is BuildConfigValue.Expression -> defaultValue
                            else -> BuildConfigValue.Literal(defaultValue)
                        }

                        else -> value
                    }

                    expectField.tags.add(BuildConfigKotlinGenerator.TagExpect)

                    if (spec.buildConfigFields.names.contains(expectField.name)) continue

                    spec.buildConfigField(expectField).configure {
                        it.value.value(expectDefault)
                        it.tags.add(BuildConfigKotlinGenerator.TagActual)
                    }
                }
            }
        }

    }

}
