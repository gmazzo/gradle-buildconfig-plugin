package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigJavaGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
open class BuildConfigTask : DefaultTask(), BuildConfigTaskSpec {

    @Input
    override var className = "BuildConfig"

    @Input
    override var packageName = ""

    @Internal
    override var fields: Collection<BuildConfigField> = emptyList()

    @Internal
    var generator: BuildConfigGenerator? = null
        get() = field ?: BuildConfigJavaGenerator

    @get:Input
    private val generatorProperty
        get() = generator!!::class.java

    @get:Input
    private val fieldsProperty
        get() = fields.map { it.toString() }

    @OutputDirectory
    override lateinit var outputDir: File

    init {
        onlyIf { fields.isNotEmpty() }
    }

    @TaskAction
    protected fun generateBuildConfigFile() {
        generator!!.execute(this)
    }

}
