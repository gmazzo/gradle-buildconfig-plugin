package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigTask
import com.github.gmazzo.buildconfig.internal.BuildConfigExtensionInternal
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.api.tasks.TaskProvider

internal object AndroidBinder {

    private val variantNameRegex = Regex("^(.+?)(UnitTest|AndroidTest)?$")

    fun Project.configure(extension: BuildConfigExtensionInternal) {
        val isKMP by lazy { isKotlinMultiplatform }

        afterEvaluate {
            check(isKMP == isKotlinMultiplatform) {
                """
                Kotlin Multiplatform plugin was applied after Android plugin.
                This is a configuration error for BuildConfig plugin since it can't determine correctly the main source set name, either `main` or `androidMain`.
                Please make sure both KMP and AGP plugins are applied before the BuildConfig one to continue with the build
            """.trimIndent()
            }
        }

        fun nameOf(name: String) =
            if (isKMP) kmpNameOf(name) else name

        androidSourceSets.all {
            val spec = extension.sourceSets.maybeCreate(nameOf(it.name))

            (it as ExtensionAware).registerExtension(spec)
        }

        androidComponents.onVariants { variant ->
            variant.bindToSourceSet(
                extension, ::nameOf,
                sequenceOf(
                    MAIN_SOURCE_SET_NAME,
                    variant.buildType,
                    variant.flavorName,
                ) + variant.productFlavors.asSequence().map { (_, flavor) -> flavor })

            variant.unitTest?.bindToSourceSet(
                extension,
                ::nameOf,
                sequenceOf(if (isKMP) "unitTest" else TEST_SOURCE_SET_NAME)
            )
            variant.androidTest?.bindToSourceSet(
                extension,
                ::nameOf,
                sequenceOf(if (isKMP) "instrumentedTest" else "androidTest")
            )
        }

        androidComponents.finalizeDsl {
            if (isKMP) {
                with(extension.sourceSets) {
                    // makes `androidMain` depend on `main`
                    maybeCreate(kmpNameOf(MAIN_SOURCE_SET_NAME))
                        .dependsOn(maybeCreate(MAIN_SOURCE_SET_NAME))

                    // makes `androidUnitTest` depend on `test`
                    maybeCreate(kmpNameOf(TEST_SOURCE_SET_NAME))
                        .dependsOn(maybeCreate(TEST_SOURCE_SET_NAME))
                }
            }
        }
    }

    private fun Any/*Component*/.bindToSourceSet(
        extension: BuildConfigExtensionInternal,
        nameOf: (String) -> String,
        extras: Sequence<String?>,
    ) {
        val ss = extension.sourceSets.maybeCreate(nameOf(this@bindToSourceSet.name!!))
        val extraSSs = extras
            .filter { !it.isNullOrBlank() }
            .map { nameOf(it!!) }
            .filter { it != ss.name }
            .toSet()
            .map(extension.sourceSets::maybeCreate)

        extraSSs.forEach(ss::dependsOn)

        for (extraSS in extraSSs) {
            extraSS.generateTask.configure {
                it.onlyIf("It was superseded by ${ss.name} source set") { false }
            }
            ss.generateTask.configure {
                it.bindTo(extraSS)
            }
        }

        sourcesJavaAddGeneratedSourceDirectory(ss.generateTask, BuildConfigTask::outputDir)
    }

    private val Project.androidComponents: ExtensionAware
        get() = extensions.getByName("androidComponents") as ExtensionAware

    private fun ExtensionAware/*AndroidComponentsExtension*/.onVariants(onVariant: Action<Any>) {
        val selectorsAll = with(javaClass.getMethod("selector").invoke(this)) {
            javaClass.getMethod("all").invoke(this)
        }
        val selectorInterface = selectorsAll.javaClass.superclass.interfaces[0]

        @Suppress("UNCHECKED_CAST")
        javaClass.getMethod("onVariants", selectorInterface, Action::class.java)
            .invoke(this, selectorsAll, onVariant)
    }

    private fun ExtensionAware/*AndroidComponentsExtension*/.finalizeDsl(callback: (Any/*AndroidBaseExtension*/) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        javaClass.getMethod("finalizeDsl", Closure::class.java)
            .invoke(this, closureOf(callback))
    }

    private val Any/*Variant*/.unitTest
        get() = try {
            javaClass.getMethod("getUnitTest").invoke(this)

        } catch (_: NoSuchMethodException) {
            null
        }

    private val Any/*Variant*/.androidTest
        get() = try {
            javaClass.getMethod("getAndroidTest").invoke(this)

        } catch (_: NoSuchMethodException) {
            null
        }

    private val Project.isKotlinMultiplatform
        get() = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

    private val Any/*Component*/.name
        get() = javaClass.getMethod("getName").invoke(this) as String?

    private val Any/*Component*/.buildType
        get() = javaClass.getMethod("getBuildType").invoke(this) as String?

    private val Any/*Component*/.flavorName
        get() = javaClass.getMethod("getFlavorName").invoke(this) as String?

    @Suppress("UNCHECKED_CAST")
    private val Any/*Component*/.productFlavors
        get() = javaClass.getMethod("getProductFlavors").invoke(this) as List<Pair<String, String>>

    // Component.sources.java!!.addGeneratedSourceDirectory
    private fun <Type : Task> Any/*Component*/.sourcesJavaAddGeneratedSourceDirectory(
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

    private fun kmpNameOf(name: String): String {
        if (name == MAIN_SOURCE_SET_NAME) return "androidMain"
        if (name == TEST_SOURCE_SET_NAME) return "androidUnitTest"

        val (variant, subVariant) = variantNameRegex.matchEntire(name)!!.destructured // this can never mismatch

        val suffix = variant.replaceFirstChar { it.uppercaseChar() }
        return when (subVariant) {
            "UnitTest" -> return "androidUnitTest$suffix"
            "AndroidTest" -> return "androidInstrumentedTest$suffix"
            else -> "android$suffix"
        }
    }

    private fun <T> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
        KotlinClosure1(action, this, this)

    /**
     * https://github.com/gradle/gradle/blob/cc8a80a2497e4bc05a2b2c6cc264a990de88beb4/platforms/core-configuration/kotlin-dsl/src/main/kotlin/org/gradle/kotlin/dsl/GroovyInteroperability.kt#L76-L95
     */
    private class KotlinClosure1<in T : Any?, V : Any>(
        val function: T.() -> V?,
        owner: Any? = null,
        thisObject: Any? = null
    ) : Closure<V?>(owner, thisObject) {

        @Suppress("unused") // to be called dynamically by Groovy
        fun doCall(it: T): V? = it.function()
    }
}
