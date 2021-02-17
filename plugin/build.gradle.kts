plugins {
    `java-gradle-plugin`
    kotlin("jvm") version embeddedKotlinVersion
    id("com.gradle.plugin-publish") version "0.11.0"
    jacoco
    id("pl.droidsonroids.jacoco.testkit") version "1.0.3"
}

apply(from = "../build.shared.gradle.kts")

base.archivesBaseName = "gradle-buildconfig-plugin"

dependencies {
    implementation(gradleApi())
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
        artifactId = base.archivesBaseName
    }
}

tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

tasks.named("check") {
    dependsOn("jacocoTestReport")
}
