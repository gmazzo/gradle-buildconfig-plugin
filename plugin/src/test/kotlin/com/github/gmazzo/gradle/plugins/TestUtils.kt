package com.github.gmazzo.gradle.plugins

import java.io.File

internal object Resources

fun readCompileOnlyClasspath() = Resources.javaClass
    .getResource("/compileOnly-classpath.txt")
    .readText()
    .split("\n")
    .map { File(it).absoluteFile }
