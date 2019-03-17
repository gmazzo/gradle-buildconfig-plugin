import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    id ("jacoco")
    id("org.jetbrains.kotlin.jvm")
    id("com.gradle.plugin-publish") version "0.10.1"
}

apply(from = "../build.shared.gradle.kts")

base.archivesBaseName = "gradle-buildconfig-plugin"

tasks {
    withType(KotlinCompile::class).all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))

    implementation(kotlin("stdlib"))
    implementation("com.squareup:javapoet:1.11.1")
    implementation("com.squareup:kotlinpoet:1.0.1")
    implementation("org.apache.commons:commons-lang3:3.8.1")
}

val pluginId = "com.github.gmazzo.buildconfig"
val repoName = "gradle-buildconfig-plugin"
val repoDesc =
    "A plugin for generating BuildConstants for any kind of Gradle projects: Java, Kotlin, Groovy, etc. Designed for KTS scripts."
val repoUrl = "https://github.com/gmazzo/$repoName"
val repoTags = listOf("buildconfig", "java", "kotlin", "gradle", "gradle-plugin", "gradle-kotlin-dsl")

gradlePlugin {
    plugins {
        create("buildconfig") {
            id = "com.github.gmazzo.buildconfig"
            displayName = "Gradle BuildConfig Plugin"
            implementationClass = "com.github.gmazzo.gradle.plugins.BuildConfigPlugin"
        }
    }
}

pluginBundle {
    website = repoUrl
    vcsUrl = repoUrl
    description = repoDesc
    tags = repoTags

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = base.archivesBaseName
    }
}

val processTestResources by tasks

task("generateCompileOnlyClasspathForTests") {
    val outFile = file("$buildDir/generated/test/resources/compileOnly-classpath.txt")
    val outDir = outFile.parentFile

    doFirst {
        outDir.mkdirs()
        outFile.writeText(configurations["compileOnly"].files.joinToString(separator = "\n"))
    }

    sourceSets["test"].resources.srcDir(outDir)
    processTestResources.dependsOn(this)
}

task("verify") {
    dependsOn("jacocoTestReport")
}

tasks.withType(JacocoReport::class.java) {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }

    tasks["check"].dependsOn(this)
}
