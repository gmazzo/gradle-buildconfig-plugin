package com.github.gmazzo.buildconfig.internal.bindings

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
                kotlin.targets.all { target ->
                    if (target.name == METADATA_TARGET_NAME) return@all

                    target.compilations.all { compilation ->
                        val targetSpec = extension.sourceSets.getByName(nameOf(compilation.defaultSourceSet.name))

                        // `computeExpectsActuals` uses this to infer the effective target for actuals
                        targetSpec.markAsKMPTarget()

                        // adds a default `dependsOn` to either `main` or `test` source set (in case `applyDefaultHierarchyTemplate` was not used)
                        when (compilation.name) {
                            KotlinCompilation.MAIN_COMPILATION_NAME -> MAIN_SOURCE_SET_NAME
                            KotlinCompilation.TEST_COMPILATION_NAME -> TEST_SOURCE_SET_NAME
                            else -> null
                        }?.let { targetSpec.dependsOn(extension.sourceSets.getByName(it)) }
                    }
                }

                extension.computeExpectsActuals()
            }
        }

    }

}
