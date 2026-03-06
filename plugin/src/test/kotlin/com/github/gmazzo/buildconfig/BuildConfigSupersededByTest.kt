package com.github.gmazzo.buildconfig

import com.github.gmazzo.buildconfig.internal.BuildConfigSourceSetInternal
import com.github.gmazzo.buildconfig.internal.DefaultBuildConfigSourceSet
import com.github.gmazzo.buildconfig.internal.capitalized
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class BuildConfigSupersededByTest {
    private val project = ProjectBuilder.builder().build()

    private fun createSourceSet(name: String): BuildConfigSourceSetInternal = with(project) {
        objects.newInstance<DefaultBuildConfigSourceSet>(name).apply {
            generateTask = tasks.register<BuildConfigTask>("generate${name.capitalized}BuildConfigClasses") {
                outputDir.set(layout.buildDirectory.dir("generated/sources/buildConfig/$name"))
            }
        }
    }

    @Test
    fun `supersededBy with forClass should not fail when task is realized`() {
        val parent = createSourceSet("test")
        val child = createSourceSet("testDebug")

        parent.forClass("MyBuildConfig")
        parent.supersededBy(child)

        assertDoesNotThrow { parent.generateTask.get() }
    }
}
