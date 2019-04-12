package com.github.gmazzo.gradle.plugins

import java.io.File
import java.io.Serializable

interface BuildConfigTaskSpec : Serializable {

    val className: String

    val packageName: String

    val fields: Collection<BuildConfigField>

    val addGeneratedAnnotation: Boolean

    val outputDir: File

}
