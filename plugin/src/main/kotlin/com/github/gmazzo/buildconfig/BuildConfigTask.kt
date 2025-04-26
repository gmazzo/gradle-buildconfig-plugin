@file:Suppress("LeakingThis")

package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.generators.BuildConfigGenerator
import com.github.gmazzo.buildconfig.generators.BuildConfigGeneratorSpec
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GradleVersion

@CacheableTask
abstract class BuildConfigTask : DefaultTask() {

    @get:Nested
    abstract val specs: SetProperty<BuildConfigClassSpec>

    @get:Nested
    abstract val generator: Property<BuildConfigGenerator>

    @get:Input
    @Suppress("unused")
    protected val generatorClass: Provider<Class<out BuildConfigGenerator>> =
        generator.map { it::class.java }

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        if (GradleVersion.current() >= GradleVersion.version("7.6")) {
            onlyIf("There are build config fields to generate", HasFields)

        } else {
            onlyIf(HasFields)
        }
    }

    @TaskAction
    fun generateBuildConfigFile() {
        val dir = outputDir.get().asFile
        dir.deleteRecursively()

        val generator = generator.get()

        specs.get().asSequence().filter { it.buildConfigFields.isNotEmpty() }.forEach {
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
                    documentation = it.documentation.orNull,
                    fields = it.buildConfigFields.sortedWith { a, b ->
                        when (val cmp = a.position.getOrElse(0).compareTo(b.position.getOrElse(0))) {
                            0 -> a.name.compareTo(b.name)
                            else -> cmp
                        }
                    },
                    outputDir = dir.also(File::mkdirs)
                )
            )
        }
    }

    private object HasFields : Spec<Task> {
        override fun isSatisfiedBy(task: Task): Boolean =
            (task as BuildConfigTask).specs.get().any { fields -> fields.buildConfigFields.isNotEmpty() }
    }

}
