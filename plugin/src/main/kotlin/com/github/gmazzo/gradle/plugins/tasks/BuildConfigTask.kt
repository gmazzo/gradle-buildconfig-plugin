package com.github.gmazzo.gradle.plugins.tasks

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class BuildConfigTask : DefaultTask() {

    @Input
    var className = "BuildConfig"

    @Input
    var packageName = ""

    @Input
    lateinit var fields: Iterable<BuildConfigField>

    @Input
    var language = BuildConfigLanguage.JAVA

    @OutputDirectory
    lateinit var outputDir: File

    internal val distinctFields
        get() = fields
            .map { it.name to it }
            .toMap()
            .values

    @TaskAction
    fun generateBuildConfigFile() {
        when (language) {
            BuildConfigLanguage.JAVA -> BuildConfigJavaGenerator
            BuildConfigLanguage.KOTLIN -> BuildConfigKoltinGenerator
        }(this)
    }

}
