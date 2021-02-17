package com.github.gmazzo.gradle.plugins.internal.bindings

import com.github.gmazzo.gradle.plugins.BuildConfigClassSpec
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal val Project.kotlinExtension
    get() = extensions.getByType(KotlinProjectExtension::class.java)

internal abstract class KotlinBindingHandler : PluginBindingHandler {

    abstract val KotlinSourceSet.compileTaskName: String

    internal val String.taskPrefix
        get() = takeUnless { it.equals("main", ignoreCase = true) }?.capitalize() ?: ""

    override fun invoke(project: Project, extension: BuildConfigExtension, sourceSetProvider: SourceSetProvider) {
        extension.useKotlinOutput()

        project.kotlinExtension.sourceSets.all { ss ->
            sourceSetProvider(ss.name) { project.bindSpec(it, ss) }
        }
    }

    private fun Project.bindSpec(spec: BuildConfigClassSpec, sourceSet: KotlinSourceSet) {
        with(spec.generateTask) {
            sourceSet.kotlin.srcDir(outputDir)

            // FIXME find a way to hook on task creating eagerly and not rely on `afterEvaluate`
            //  https://github.com/gmazzo/gradle-buildconfig-plugin/issues/7
            afterEvaluate {
                tasks.named(sourceSet.compileTaskName) {
                    it.dependsOn(this@with)
                }
            }
        }
    }

}
