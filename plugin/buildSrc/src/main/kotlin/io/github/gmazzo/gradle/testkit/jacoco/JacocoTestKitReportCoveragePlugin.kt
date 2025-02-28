package io.github.gmazzo.gradle.testkit.jacoco

import io.github.gmazzo.gradle.testkit.jacoco.DumpAction.Companion.dumpOnBuildFinished
import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion
import org.jacoco.agent.rt.RT
import javax.inject.Inject

class JacocoTestKitReportCoveragePlugin @Inject constructor(
    private val gradle: Gradle,
) : Plugin<Any> {

    override fun apply(target: Any) {
        if (GradleVersion.current() >= GradleVersion.version("8.1")) {
            gradle.dumpOnBuildFinished()

        } else {
            gradle.buildFinished {
                RT.getAgent().dump(true)
            }
        }
    }

}
