package com.github.gmazzo.buildconfig.internal.bindings

import com.github.gmazzo.buildconfig.BuildConfigTask
import com.github.gmazzo.buildconfig.internal.BuildConfigExtensionInternal
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder.registerExtension
import com.github.gmazzo.buildconfig.internal.capitalized
import groovy.lang.Closure
import java.lang.reflect.InvocationTargetException
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
import org.gradle.kotlin.dsl.closureOf

internal object AndroidBinder {
    private const val ANDROID_TEST_SOURCE_SET_NAME = "androidTest"
    private val variantNameRegex = Regex("^(test|androidTest|android)?(.+?)(UnitTest|AndroidTest)?$")

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

        if (plugins.hasPlugin("com.android.base")) {
            androidSourceSets.all {
                val spec = extension.sourceSets.maybeCreate(nameOf(it.name))

                (it as ExtensionAware).registerExtension(spec)
            }
        }

        val main by lazy { extension.sourceSets.maybeCreate(nameOf(MAIN_SOURCE_SET_NAME)) }
        val test by lazy { extension.sourceSets.maybeCreate(nameOf(TEST_SOURCE_SET_NAME)) }
        val androidTest by lazy { extension.sourceSets.maybeCreate(nameOf(ANDROID_TEST_SOURCE_SET_NAME)) }

        androidComponents.onVariants { variant ->
            variant.bindToSourceSet(extension, main, ::nameOf)
            variant.unitTest?.bindToSourceSet(extension, test, ::nameOf) {
                nameOf("test${it.capitalized}")
            }
            variant.androidTest?.bindToSourceSet(extension, androidTest, ::nameOf) {
                nameOf("androidTest${it.capitalized}")
            }
        }

        androidComponents.finalizeDsl {
            if (isKMP) {
                // makes `androidMain` and `androidTest` depend on `main` and `test`
                main.dependsOn(extension.sourceSets.maybeCreate(MAIN_SOURCE_SET_NAME))
                test.dependsOn(extension.sourceSets.maybeCreate(TEST_SOURCE_SET_NAME))
            }
        }
    }

    private fun Any/*Component*/.bindToSourceSet(
        extension: BuildConfigExtensionInternal,
        mainSpec: BuildConfigSourceSetInternal,
        namer: (String) -> String,
        variantNamer: (String) -> String = namer,
    ) {
        val spec = extension.sourceSets.maybeCreate(namer(this@bindToSourceSet.name))
        val supersededSpecs = linkedSetOf<BuildConfigSourceSetInternal>()

        val flavorsSpecs = productFlavors
            .map { (_, flavor) -> extension.sourceSets.maybeCreate(variantNamer(flavor)) }
            .also { it.forEach(spec::dependsOn) }
            .reversed()

        buildType?.let {
            val btSpec = extension.sourceSets.maybeCreate(variantNamer(it))

            btSpec.dependsOn(mainSpec)
            supersededSpecs.add(btSpec)
        }
        flavorName.takeUnless { it.isNullOrBlank() }?.let {
            val allFlavorsSpec = extension.sourceSets.maybeCreate(variantNamer(it))

            supersededSpecs.add(allFlavorsSpec)
            (flavorsSpecs - allFlavorsSpec).forEach(allFlavorsSpec::dependsOn)
        }
        supersededSpecs.addAll(flavorsSpecs)

        for (parentSpec in supersededSpecs) {
            parentSpec.dependsOn(mainSpec)

            if (parentSpec != spec) {
                spec.dependsOn(parentSpec)
                parentSpec.supersededBy(spec)
            }
        }

        if (mainSpec != spec) {
            mainSpec.supersededBy(spec)
            spec.defaultsFrom(mainSpec)
        }

        sourcesJavaAddGeneratedSourceDirectory(spec.generateTask, BuildConfigTask::outputDir)
        try {
            sourcesKotlinAddGeneratedSourceDirectory(spec.generateTask, BuildConfigTask::outputDir)
        } catch (_: InvocationTargetException) {
            // this may fail on older AGP versions
        }
    }

    private val Project.androidComponents: ExtensionAware
        get() = extensions.getByName("androidComponents") as ExtensionAware

    private fun ExtensionAware/*AndroidComponentsExtension*/.onVariants(onVariant: Action<Any>) {
        val selectorsAll = try {
            with(javaClass.getMethod("selector").invoke(this)) {
                javaClass.getMethod("all").invoke(this)
            }
        } catch (_: NoSuchMethodException) {
            null
        }

        val selectorInterface = selectorsAll?.javaClass?.superclass?.interfaces[0]

        fun callMethod(name: String) {
            @Suppress("UNCHECKED_CAST")
            javaClass.getMethod(name, *listOfNotNull(selectorInterface, Action::class.java).toTypedArray())
                .invoke(this, *listOfNotNull(selectorsAll, onVariant).toTypedArray())
        }

        try {
            callMethod("onVariants")

        } catch (ex: NoSuchMethodException) {
            try {
                callMethod("onVariant")

            } catch (_: NoSuchMethodException) {
                throw ex
            }
        }
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
        get() = javaClass.getMethod("getName").invoke(this) as String

    private val Any/*Component*/.buildType
        get() = javaClass.getMethod("getBuildType").invoke(this) as String?

    private val Any/*Component*/.flavorName
        get() = javaClass.getMethod("getFlavorName").invoke(this) as String?

    @Suppress("UNCHECKED_CAST")
    private val Any/*Component*/.productFlavors
        get() = javaClass.getMethod("getProductFlavors").invoke(this) as List<Pair<String, String>>

    // Component.sources.java?.addGeneratedSourceDirectory
    private fun <Type : Task> Any/*Component*/.sourcesJavaAddGeneratedSourceDirectory(
        task: TaskProvider<out Type>,
        wiredWith: (Type) -> DirectoryProperty,
    ) = sourcesAddGeneratedSourceDirectory("getJava", task, wiredWith)

    // Component.sources.kotlin?.addGeneratedSourceDirectory
    private fun <Type : Task> Any/*Component*/.sourcesKotlinAddGeneratedSourceDirectory(
        task: TaskProvider<out Type>,
        wiredWith: (Type) -> DirectoryProperty,
    ) = sourcesAddGeneratedSourceDirectory("getKotlin", task, wiredWith)

    private fun <Type : Task> Any/*Component*/.sourcesAddGeneratedSourceDirectory(
        into: String,
        task: TaskProvider<out Type>,
        wiredWith: (Type) -> DirectoryProperty,
    ) = with(javaClass.getMethod("getSources").invoke(this)) {
        with(javaClass.getMethod(into).invoke(this)) {
            this?.javaClass
                ?.getMethod("addGeneratedSourceDirectory", TaskProvider::class.java, Function1::class.java)
                ?.invoke(this, task, wiredWith)
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
        if (name == ANDROID_TEST_SOURCE_SET_NAME) return "androidInstrumentedTest"

        val (prefix, variant, suffix) = variantNameRegex.matchEntire(name)!!.destructured // this can never mismatch

        return when {
            prefix == "test" || suffix == "UnitTest" -> return "androidUnitTest${variant.capitalized}"
            prefix == "androidTest" || suffix == "AndroidTest" -> return "androidInstrumentedTest${variant.capitalized}"
            prefix == "android" -> name // e.g., androidMain, androidHostTest
            else -> "android${variant.capitalized}"
        }
    }

}
