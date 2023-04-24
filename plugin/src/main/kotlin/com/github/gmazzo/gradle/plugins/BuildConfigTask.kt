package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGeneratorSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*

@CacheableTask
abstract class BuildConfigTask : DefaultTask() {

    @get:Nested
    abstract val specs: SetProperty<BuildConfigClassSpec>

    @get:Internal
    abstract val generator: Property<BuildConfigGenerator>

    @get:Input
    @Suppress("unused")
    internal val generatorName
        get() = generator.javaClass

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    protected fun generateBuildConfigFile() = outputDir.get().asFile.let { dir ->
        dir.deleteRecursively()
        dir.mkdirs()

        val generator = generator.get()

        specs.get().forEach {
            val rawClassName = it.className.get()
            val (packageName, className) = when (val rawPackage = it.packageName.orNull) {
                null -> when (val i = rawClassName.lastIndexOf('.')) {
                    -1 -> "" to rawClassName
                    else -> rawClassName.substring(0, i) to rawClassName.substring(i + 1)
                }

                else -> rawPackage to rawClassName
            }

            generator.execute(
                BuildConfigGeneratorSpec(
                    className = className,
                    packageName = packageName,
                    fields = it.buildConfigFields,
                    outputDir = dir
                )
            )
        }
    }

}
