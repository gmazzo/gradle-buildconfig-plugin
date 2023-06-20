package com.github.gmazzo.gradle.plugins

import com.github.gmazzo.gradle.plugins.generators.BuildConfigGenerator
import com.github.gmazzo.gradle.plugins.generators.BuildConfigGeneratorSpec
import com.github.gmazzo.gradle.plugins.internal.DefaultBuildConfigClassSpec
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class BuildConfigTaskTest {

    private val project = ProjectBuilder.builder().build()

    private val spec: BuildConfigClassSpec = project.objects.newInstance<DefaultBuildConfigClassSpec>("spec").apply {
        className.set("aClassName")
        packageName.set("aPackage")
    }

    private val outDir = project.layout.buildDirectory.dir("outDir")

    private val generator: BuildConfigGenerator = mockk(relaxUnitFun = true)

    private val task = project.tasks.create<BuildConfigTask>("testedTask") {
        generator.set(this@BuildConfigTaskTest.generator)
        outputDir.set(outDir)
        specs.add(spec)
    }

    @Test
    fun `order of fields must be honored when propagated to the generator`() {
        val fields = sequenceOf(
            spec.buildConfigField("Int", "FIRST", "1"),
            spec.buildConfigField("Int", "SECOND", "2"),
            spec.buildConfigField("Int", "THIRD", "3"),
            spec.buildConfigField("Int", "LAST", "9"),
        ).map { it.get() }.toList()

        task.generateBuildConfigFile()

        verify {
            generator.execute(
                BuildConfigGeneratorSpec(
                    className = "aClassName",
                    packageName = "aPackage",
                    fields = fields,
                    outputDir = outDir.get().asFile.absoluteFile,
                )
            )
        }
    }

}