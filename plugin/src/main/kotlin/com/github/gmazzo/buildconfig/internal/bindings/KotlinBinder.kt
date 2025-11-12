package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigClassSpec
import com.github.gmazzo.buildconfig.BuildConfigField
import com.github.gmazzo.buildconfig.BuildConfigValue
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.github.gmazzo.buildconfig.internal.BuildConfigExtensionInternal
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
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
        get() = extensions.getByName("kotlin") as ExtensionAware

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

        private fun nameOf(name: String): String = when (name) {
            COMMON_MAIN_SOURCE_SET_NAME -> MAIN_SOURCE_SET_NAME
            COMMON_TEST_SOURCE_SET_NAME -> TEST_SOURCE_SET_NAME
            else -> name
        }

        fun Project.configure(extension: BuildConfigExtensionInternal) {
            with(KotlinBinder) { configure(extension, ::nameOf) }

            afterEvaluate {
                kotlin.sourceSets.all { ss ->
                    val spec = extension.sourceSets.getByName(nameOf(ss.name))
                    val dependsOnSpecs = spec.allDependsOn.toSet()

                    lookForExpectFields(spec, dependsOnSpecs)
                    spec.extraSpecs.all { extra ->
                        lookForExpectFields(extra, dependsOnSpecs) {
                            (it as BuildConfigSourceSetInternal).extraSpecs.findByName(extra.name)
                        }
                    }
                }
            }
        }

        private val BuildConfigSourceSetInternal.allDependsOn: Sequence<BuildConfigSourceSetInternal>
            get() = dependsOn.asSequence() + dependsOn.asSequence().flatMap { it.allDependsOn }

        private fun lookForExpectFields(
            spec: BuildConfigClassSpec,
            dependsOnSpecs: Set<BuildConfigSourceSetInternal>,
            resolve: (BuildConfigClassSpec) -> BuildConfigClassSpec? = { it },
        ) {
            val target = resolve(spec)!!
            val targetDependsOnSpecs = dependsOnSpecs.mapNotNull(resolve).toSet()

            val expectSpecs = linkedSetOf<BuildConfigClassSpec>()
            for (field in spec.buildConfigFields) {
                val commonFields = mutableListOf<BuildConfigField>()
                val commonSpecs = mutableListOf<BuildConfigClassSpec>()
                for (dependsOnSpec in targetDependsOnSpecs) {
                    val dependsOnField = dependsOnSpec.buildConfigFields.findByName(field.name) ?: continue
                    if (dependsOnField.value.orNull !is BuildConfigValue.Expect) continue

                    commonFields.add(dependsOnField)
                    commonSpecs.add(0, dependsOnSpec)
                }
                val closestCommonSpec = commonSpecs.firstOrNull() ?: continue

                commonFields.forEach { it.tags.add(BuildConfigKotlinGenerator.TagExpect) }
                field.tags.add(BuildConfigKotlinGenerator.TagActual)

                // also makes sure that the actual class matches the expect declaration
                target.className.convention(closestCommonSpec.className)
                target.packageName.convention(closestCommonSpec.packageName)
                target.documentation.convention(closestCommonSpec.documentation)

                expectSpecs.addAll(commonSpecs)
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

    }

}
