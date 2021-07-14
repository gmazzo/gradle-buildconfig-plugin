package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGeneratorSpec
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

@CacheableTask
@Suppress("UnstableApiUsage", "LeakingThis")
open class BuildConfigTask : DefaultTask() {

    @Input
    val className: Property<String> =
        project.objects.property<String>().convention("BuildConfig")

    @Input
    val packageName: Property<String> =
        project.objects.property<String>().convention("")

    @Internal
    val fields: ListProperty<BuildConfigField> =
        project.objects.listProperty<BuildConfigField>().convention(emptyList())

    @Internal
    val generator: Property<BuildConfigGenerator> =
        project.objects.property<BuildConfigGenerator>().convention(BuildConfigJavaGenerator())

    @get:Input
    @Suppress("unused")
    internal val generatorName
        get() = generator.javaClass

    @get:Input
    @Suppress("unused")
    internal val fieldsContent
        get() = fields.map { list ->
            list.map {
                mapOf("type" to it.type, "name" to it.name, "value" to it.value.get())
            }
        }

    @OutputDirectory
    val outputDir: DirectoryProperty =
        project.objects.directoryProperty()

    init {
        onlyIf { fields.get().isNotEmpty() }
    }

    @TaskAction
    protected fun generateBuildConfigFile() = outputDir.get().asFile.let { dir ->
        dir.deleteRecursively()
        dir.mkdirs()

        generator.get().execute(
            BuildConfigGeneratorSpec(
                className = className.get(),
                packageName = packageName.get(),
                fields = fields.get(),
                outputDir = dir
            )
        )
    }

}
