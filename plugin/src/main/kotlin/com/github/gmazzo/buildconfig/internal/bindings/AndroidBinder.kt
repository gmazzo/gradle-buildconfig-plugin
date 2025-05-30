package org.gradle.kotlin.dsl.com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigTask
import com.github.gmazzo.buildconfig.generators.BuildConfigJavaGenerator
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.api.tasks.TaskProvider

internal object AndroidBinder {

    fun Project.configure(extension: BuildConfigExtension) {
        val isKMP by lazy { isKotlinMultiplatform }
        val mainSourceSetName by lazy { if (isKMP) "androidMain" else MAIN_SOURCE_SET_NAME }
        val testSourceSetName by lazy { if (isKMP) "androidTest" else TEST_SOURCE_SET_NAME }

        afterEvaluate {
            check(isKMP == isKotlinMultiplatform) {
                """
                Kotlin Multiplatform plugin was applied after Android plugin.
                This is a configuration error for BuildConfig plugin since it can't determine correctly the main source set name, either `main` or `androidMain`.
                Please make sure both KMP and AGP plugins are applied before the BuildConfig one to continue with the build
            """.trimIndent()
            }
        }

        androidSourceSets.all {
            val kmpAwareName = when (it.name) {
                MAIN_SOURCE_SET_NAME -> mainSourceSetName
                TEST_SOURCE_SET_NAME -> testSourceSetName
                else -> it.name
            }
            val spec = extension.sourceSets.maybeCreate(kmpAwareName)

            (it as ExtensionAware).registerExtension(spec)
            it.javaSrcDir(spec.generateTask.flatMap { it.outputDir })
        }

        androidComponentsOnVariants { variant ->
            val unitTest = variant.unitTest
            val androidTest = variant.androidTest

            for (component in listOfNotNull(variant, unitTest, androidTest)) {
                val specNames = when (component) {
                    variant -> sequenceOf(
                        mainSourceSetName,
                        component.name,
                        component.buildType,
                        component.flavorName
                    ) + component.productFlavors.asSequence().map { (_, flavor) -> flavor }

                    unitTest -> sequenceOf(
                        testSourceSetName,
                        component.name
                    )

                    androidTest -> sequenceOf("androidTest", component.name)
                    else -> error("Unsupported component $component")
                }
                val specs = specNames
                    .filterNotNull()
                    .filter(String::isNotBlank)
                    .toSet()
                    .map(extension.sourceSets::maybeCreate)

                for (spec in specs) {
                    component.sourcesJavaAddGeneratedSourceDirectory(spec.generateTask, BuildConfigTask::outputDir)
                }
            }
        }
    }

    // project.androidComponents.onVariants
    private fun Project.androidComponentsOnVariants(onVariant: Action<Any>) =
        with(extensions.getByName("androidComponents")) {
            val selectorsAll = with(javaClass.getMethod("selector").invoke(this)) {
                javaClass.getMethod("all").invoke(this)
            }
            val selectorInterface = selectorsAll.javaClass.superclass.interfaces[0]

            @Suppress("UNCHECKED_CAST")
            javaClass.getMethod("onVariants", selectorInterface, Action::class.java)
                .invoke(this, selectorsAll, onVariant)
        }

    // Variant.unitTest
    private val Any.unitTest
        get() = try {
            javaClass.getMethod("getUnitTest").invoke(this)

        } catch (_: NoSuchMethodException) {
            null
        }

    // Variant.androidTest
    private val Any.androidTest
        get() = try {
            javaClass.getMethod("getAndroidTest").invoke(this)

        } catch (_: NoSuchMethodException) {
            null
        }

    private val Project.isKotlinMultiplatform
        get() = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

    // Component.name
    private val Any.name
        get() = javaClass.getMethod("getName").invoke(this) as String?

    // Component.buildType
    private val Any.buildType
        get() = javaClass.getMethod("getBuildType").invoke(this) as String?

    // Component.flavorName
    private val Any.flavorName
        get() = javaClass.getMethod("getFlavorName").invoke(this) as String?

    // Component.productFlavors
    @Suppress("UNCHECKED_CAST")
    private val Any.productFlavors
        get() = javaClass.getMethod("getProductFlavors").invoke(this) as List<Pair<String, String>>

    // Component.sources.java!!.addGeneratedSourceDirectory
    private fun <Type : Task> Any.sourcesJavaAddGeneratedSourceDirectory(
        task: TaskProvider<out Type>,
        wiredWith: (Type) -> DirectoryProperty,
    ) = with(javaClass.getMethod("getSources").invoke(this)) {
        with(javaClass.getMethod("getJava").invoke(this)) {
            javaClass.getMethod("addGeneratedSourceDirectory", TaskProvider::class.java, Function1::class.java)
                .invoke(this, task, wiredWith)
        }
    }

    // project.android.sourceSets
    private val Project.androidSourceSets
        get() = with(extensions.getByName("android")) {
            @Suppress("UNCHECKED_CAST")
            javaClass.getMethod("getSourceSets")
                .invoke(this) as NamedDomainObjectContainer<Named>
        }

    // AndroidSourceSet.java.srcDir(dir)
    private fun Named.javaSrcDir(dir: Provider<Directory>) {
        with(javaClass.getMethod("getJava").invoke(this)) {
            javaClass.getMethod("srcDir", Any::class.java)
                .invoke(this, dir)
        }
    }

}
