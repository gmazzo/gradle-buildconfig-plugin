@file:Suppress("LeakingThis")

package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.generators.BuildConfigGeneratorSpec
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class BuildConfigTask : DefaultTask() {

    @get:Nested
    public abstract val specs: SetProperty<BuildConfigClassSpec>

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @TaskAction
    public fun generateBuildConfigFile() {
        val dir = outputDir.get().asFile
        dir.deleteRecursively()

        specs.get().asSequence().filter { it.buildConfigFields.isNotEmpty() }.forEach {
            val rawClassName = it.className.get()
            val (packageName, className) = when (val rawPackage = it.packageName.orNull) {
                null -> when (val i = rawClassName.lastIndexOf('.')) {
                    -1 -> "" to rawClassName
                    else -> rawClassName.take(i) to rawClassName.substring(i + 1)
                }

                else -> rawPackage to rawClassName
            }

            it.generator.get().execute(
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
