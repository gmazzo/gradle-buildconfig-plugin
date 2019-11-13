package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigOutputType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
open class BuildConfigTask : DefaultTask(), BuildConfigTaskSpec {

    @Input
    override var className = "BuildConfig"

    @Input
    override var packageName = ""

    @Input
    override lateinit var fields: Collection<BuildConfigField>

    @Input
    var outputType: BuildConfigGenerator = BuildConfigOutputType.JAVA

    @OutputDirectory
    override lateinit var outputDir: File

    init {
        onlyIf { fields.isNotEmpty() }
    }

    @TaskAction
    protected fun generateBuildConfigFile() {
        outputType.execute(this)
    }

}
