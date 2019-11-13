package com.github.gmazzo.gradle.plugins

import java.io.File

interface BuildConfigTaskSpec {

    val className: String

    val packageName: String

    val fields: Collection<BuildConfigField>

    val outputDir: File

}
