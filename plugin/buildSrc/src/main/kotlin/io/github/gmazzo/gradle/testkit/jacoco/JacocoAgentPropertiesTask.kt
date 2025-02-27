package io.github.gmazzo.gradle.testkit.jacoco

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Uses an absolut file path")
abstract class JacocoAgentPropertiesTask : DefaultTask() {

    @get:Internal
    abstract val jacocoExecFile: RegularFileProperty

    @get:Input
    val jacocoExecFilePath = jacocoExecFile.asFile.map { it.absolutePath }

    @get:OutputDirectory
    abstract val generatedResourcesDir: DirectoryProperty

    init {
        with(project) {
            generatedResourcesDir
                .convention(layout.dir(provider { temporaryDir }))
        }
    }

    @TaskAction
    fun generateAgentProperties(): Unit = generatedResourcesDir.get().asFile
        .apply { deleteRecursively(); mkdirs() }
        .resolve("jacoco-agent.properties")
        .writer()
        .use { it.appendLine("destfile=${jacocoExecFilePath.get()}") }

}
