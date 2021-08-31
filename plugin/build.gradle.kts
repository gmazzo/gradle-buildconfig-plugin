import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.Thread.sleep

plugins {
    `java-gradle-plugin`
    `maven-publish`
    jacoco
    kotlin("jvm") version embeddedKotlinVersion
    id("com.glovoapp.semantic-versioning") version "1.1.0"
    id("com.gradle.plugin-publish") version "0.11.0"
    id("pl.droidsonroids.jacoco.testkit") version "1.0.8"
}

apply(from = "../build.shared.gradle.kts")

base.archivesName.set("gradle-buildconfig-plugin")

semanticVersion {
    propertiesFile.set(file("../version.properties"))
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin"))
    implementation("com.squareup:javapoet:1.11.1")
    implementation("com.squareup:kotlinpoet:1.0.1")
    implementation("org.apache.commons:commons-lang3:3.8.1")

    testImplementation(gradleTestKit())
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
        artifactId = base.archivesName.get()
    }
}

tasks {

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            apiVersion = "1.3"
        }
    }

    withType<Test> {
        environment("test.tmpDir", temporaryDir)
    }

    withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        doFirst {
            // sometimes fails with "Unable to read execution data file build/jacoco/test.exec"
            sleep(1000)
        }
    }

    named("check") {
        dependsOn("jacocoTestReport")
    }

}
