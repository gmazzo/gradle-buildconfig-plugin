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
@Suppress("LeakingThis")
abstract class BuildConfigTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val className: Property<String>

    @get:Input
    @get:Optional
    abstract val packageName: Property<String>

    @get:Internal
    abstract val fields: ListProperty<BuildConfigField>

    @get:Internal
    abstract val generator: Property<BuildConfigGenerator>

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

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        className.convention("BuildConfig")
        packageName.convention("")
        generator.convention(BuildConfigJavaGenerator())
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
