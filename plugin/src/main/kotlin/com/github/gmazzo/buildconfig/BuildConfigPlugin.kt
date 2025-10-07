package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.generators.BuildConfigJavaGenerator
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.buildconfig.internal.DefaultBuildConfigExtension
import com.github.gmazzo.buildconfig.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.buildconfig.internal.bindings.JavaBinder
import com.github.gmazzo.buildconfig.internal.bindings.KotlinBinder
import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.com.github.gmazzo.buildconfig.internal.bindings.AndroidBinder
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion

class BuildConfigPlugin : Plugin<Project> {

    companion object {
        const val MIN_GRADLE_VERSION = "7.3"
    }

    override fun apply(project: Project) = with(project) {
        check(GradleVersion.current() >= GradleVersion.version(MIN_GRADLE_VERSION)) {
            "Gradle version must be at least $MIN_GRADLE_VERSION"
        }

        val sourceSets = objects.domainObjectContainer(DefaultBuildConfigSourceSet::class)

        val defaultSS = sourceSets.create(SourceSet.MAIN_SOURCE_SET_NAME)

        val extension = extensions.create(
            BuildConfigExtension::class.java,
            "buildConfig",
            DefaultBuildConfigExtension::class.java,
            sourceSets,
            defaultSS,
        )

        sourceSets.configureEach { configureSourceSet(it, defaultSS) }

        extension.generateAtSync
            .convention(
                providers.gradleProperty("com.github.gmazzo.buildconfig.generateAtSync")
                    .map { it.toBoolean() }
                    .orElse(true)
            )
            .finalizeValueOnRead()

        afterEvaluate {
            sourceSets.toList() // force configuration

            // generate at sync
            if (extension.generateAtSync.get() && isGradleSync) {
                tasks.maybeCreate("prepareKotlinIdeaImport").dependsOn(tasks.withType<BuildConfigTask>())
            }
        }

        plugins.withId("java") {
            with(JavaBinder) { configure(extension) }
        }

        plugins.withAnyId(
            "org.jetbrains.kotlin.jvm",
            "org.jetbrains.kotlin.js",
        ) {
            with(KotlinBinder) { configure(extension) }
        }

        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            with(KotlinBinder.Multiplatform) { configure(extension) }
        }

        plugins.withId("com.android.base") {
            with(AndroidBinder) { configure(extension) }
        }
    }

    private val isGradleSync
        get() = System.getProperty("idea.sync.active") == "true"

    private fun Project.configureSourceSet(
        sourceSet: BuildConfigSourceSetInternal,
        defaultSS: BuildConfigSourceSetInternal,
    ) {
        check(sourceSet.name.matches("[\\w-]+".toRegex())) {
            "Invalid name '${sourceSet.name}': only alphanumeric characters are allowed"
        }

        val prefix = when (sourceSet) {
            defaultSS -> ""
            else -> sourceSet.name.replace("[_-]".toRegex(), "").replaceFirstChar { it.titlecaseChar() }
        }
        val taskPrefix = if (plugins.hasPlugin("com.android.base")) "NonAndroid" else ""

        sourceSet.className
            .convention("${prefix}BuildConfig")
            .finalizeValueOnRead()

        sourceSet.packageName
            .convention(
                when (sourceSet) {
                    defaultSS -> defaultPackage.map(String::javaIdentifier)
                    else -> defaultSS.packageName
                }
            )
            .finalizeValueOnRead()

        sourceSet.generator
            .convention(
                when (sourceSet) {
                    defaultSS -> provider {
                        if (hasKotlinPlugin()) BuildConfigKotlinGenerator()
                        else BuildConfigJavaGenerator()
                    }

                    else -> defaultSS.generator
                }
            )
            .finalizeValueOnRead()

        sourceSet.extraSpecs.configureEach {

            it.generator
                .convention(sourceSet.generator)
                .finalizeValueOnRead()

            it.className
                .finalizeValueOnRead()

            it.packageName
                .convention(sourceSet.packageName)
                .finalizeValueOnRead()

            it.documentation
                .finalizeValueOnRead()

            configureFields(it.buildConfigFields)

        }

        configureFields(sourceSet.buildConfigFields)

        sourceSet.generateTask = tasks.register<BuildConfigTask>("generate${prefix}${taskPrefix}BuildConfig") {
            group = "BuildConfig"
            description = "Generates the build constants class for '${sourceSet.name}' source"

            outputDir.set(layout.buildDirectory.dir("generated/sources/buildConfig/${sourceSet.name}"))

            specs.add(provider { isolate(sourceSet) })
            specs.addAll(provider { sourceSet.extraSpecs.map { isolate(it) } })
        }
    }

    private fun configureFields(fields: DomainObjectCollection<BuildConfigField>) = fields.configureEach {
        it.type.finalizeValueOnRead()
        it.value.finalizeValueOnRead()
        it.position.finalizeValueOnRead()
    }

    private fun Project.hasKotlinPlugin() = listOf(
        "org.jetbrains.kotlin.android",
        "org.jetbrains.kotlin.js",
        "org.jetbrains.kotlin.jvm",
        "org.jetbrains.kotlin.multiplatform",
    ).any(plugins::hasPlugin)

    /**
     * Helper method to create a copy of a BuildConfigClassSpec not linked to Gradle related extensions/DSL
     * It makes it compatible with Configuration Cache
     */
    private fun Project.isolate(source: BuildConfigClassSpec) =
        objects.newInstance<BuildConfigClassSpec>(source.name).apply spec@{
            generator.value(source.generator).disallowChanges()
            className.value(source.className).disallowChanges()
            packageName.value(source.packageName).disallowChanges()
            documentation.value(source.documentation).disallowChanges()
            buildConfigFields.addAll(source.buildConfigFields.map { field ->
                objects.newInstance<BuildConfigField>(field.name).apply field@{
                    this@field.type.value(field.type).disallowChanges()
                    this@field.value.value(field.value).disallowChanges()
                    this@field.position.value(field.position).disallowChanges()
                }
            })
        }

    private val Project.defaultPackage
        get() = provider {
            group
                .toString()
                .takeUnless { it.isEmpty() }
                ?.let { "$it.${project.name}" }
                ?: project.name
        }

    @Suppress("SameParameterValue")
    private fun PluginContainer.withAnyId(vararg ids: String, action: Action<in Plugin<*>>) {
        ids.forEach { withId(it, action) }
    }

}
