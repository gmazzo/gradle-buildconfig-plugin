package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.gradle.testing.jacoco.plugins.JacocoPlugin.ANT_CONFIGURATION_NAME
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

class JacocoGradleTestKitPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "java-gradle-plugin")
        apply(plugin = "jacoco")

        val jacoco = the<JacocoPluginExtension>()

        val jacocoRuntime by configurations.registering {
            defaultDependencies {
                add(project.dependencies.create("org.jacoco:org.jacoco.agent:${jacoco.toolVersion}:runtime"))
            }
        }

        val instrumentationTask =
            tasks.register<JacocoInstrumentationTask>("instrumentPluginClassesForJaCoCo") {
                jacocoClasspath.from(configurations.named(ANT_CONFIGURATION_NAME))
                instrumentedClassesDir.convention(layout.buildDirectory.dir("jacoco/instrumentedPluginClasses"))
            }

        val propertiesTask = tasks.register<JacocoAgentPropertiesTask>("generateJaCoCoAgentPropertiesForTestKit") {
            jacocoExecFile = tasks.getByName(TEST_TASK_NAME).the<JacocoTaskExtension>().destinationFile
        }

        afterEvaluate {
            tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
                val it = instrumentationTask.get()
                it.dependsOn(dependsOn)
                it.classpath.from(pluginClasspath.from.toList())

                pluginClasspath.setFrom(
                    it.instrumentedClassesDir,
                    it.classpath,
                    propertiesTask,
                    jacocoRuntime,
                    JacocoGradleTestKitPlugin::class.java.jarFile,
                )
            }
        }

    }

    private val Class<*>.jarFile
        get() = protectionDomain.codeSource.location

}
