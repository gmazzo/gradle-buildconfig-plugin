package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGeneratorSpec
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

@CacheableTask
@Suppress("UnstableApiUsage", "LeakingThis")
open class BuildConfigTask : DefaultTask() {

    @Input
    val className: Property<String> =
        project.objects.property(String::class.java).convention("BuildConfig")

    @Input
    val packageName: Property<String> =
        project.objects.property(String::class.java).convention("")

    @Internal
    val fields: ListProperty<BuildConfigField> =
        project.objects.listProperty(BuildConfigField::class.java).convention(emptyList<BuildConfigField>())

    @Internal
    val generator: Property<BuildConfigGenerator> =
        project.objects.property(BuildConfigGenerator::class.java).convention(BuildConfigJavaGenerator())

    @get:Input
    @Suppress("unused")
    internal val generatorName
        get() = generator.javaClass

    @get:Input
    @Suppress("unused")
    internal val fieldsContent
        get() = fields.map { it.toString() }

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
