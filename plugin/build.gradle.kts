import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.jacoco.testkit)
}

group = "com.github.gmazzo.buildconfig"
description = "Gradle BuildConfig Plugin"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    fun DependencyHandler.plugin(dependency: ExternalModuleDependencyFactory.PluginNotationSupplier) =
        plugin(dependency.asProvider())

    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.kotlin))

    implementation(libs.javapoet)
    implementation(libs.kotlinpoet)
    implementation(libs.apache.commons.lang)

    testImplementation(gradleTestKit())
    testImplementation(libs.kotlin.test)
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

gradlePlugin {
    vcsUrl.set("https://github.com/gmazzo/gradle-buildconfig-plugin")
    website.set(vcsUrl)

    plugins {
        create("buildconfig") {
            id = "com.github.gmazzo.buildconfig"
            displayName = name
            implementationClass = "com.github.gmazzo.gradle.plugins.BuildConfigPlugin"
            description =
                "A plugin for generating BuildConstants for any kind of Gradle projects: Java, Kotlin, Groovy, etc. Designed for KTS scripts."
            tags.addAll("buildconfig", "java", "kotlin", "kotlin-multiplatform")
        }
    }
}

tasks.withType<Test> {
    workingDir = temporaryDir
    useJUnitPlatform()
}

tasks.check {
    dependsOn("jacocoTestReport")
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
