package com.github.gmazzo.gradle.plugins.tasks

import com.github.gmazzo.gradle.plugins.BuildConfigField
import com.github.gmazzo.gradle.plugins.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.BuildConfigLanguage
import com.github.gmazzo.gradle.plugins.BuildConfigTaskSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class BuildConfigTask : DefaultTask(), BuildConfigTaskSpec {

    @Input
    override var className = "BuildConfig"

    @Input
    override var packageName = ""

    @Input
    override lateinit var fields: Collection<BuildConfigField>

    @Input
    var language: BuildConfigGenerator = BuildConfigLanguage.JAVA

    @Input
    override var addGeneratedAnnotation = true

    @OutputDirectory
    override lateinit var outputDir: File

    init {
        onlyIf { fields.isNotEmpty() }
    }

    @TaskAction
    fun generateBuildConfigFile() {
        language.execute(this)
    }

}
