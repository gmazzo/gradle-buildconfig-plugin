package com.github.gmazzo.gradle.plugins.generators

enum class BuildConfigOutputType(
    generator: BuildConfigGenerator
) : BuildConfigGenerator by generator {

    JAVA(BuildConfigJavaGenerator),

    KOTLIN(BuildConfigKotlinObjectGenerator),

    KOTLIN_FILE(BuildConfigKotlinFileGenerator);

    companion object {

        private val String.onlyLetters get() = replace("[\\W-_]+".toRegex(), "")

        fun String.asOutputType() = values()
            .firstOrNull { it.name.onlyLetters.equals(onlyLetters, ignoreCase = true) }
            ?: valueOf(this)

    }

}
