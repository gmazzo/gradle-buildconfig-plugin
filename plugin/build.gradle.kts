import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.jacoco.testkit)
    alias(libs.plugins.publicationsReport)
}

group = "com.github.gmazzo.buildconfig"
description = "Gradle BuildConfig Plugin"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all",)
    }
}

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    fun DependencyHandler.plugin(dependency: ExternalModuleDependencyFactory.PluginNotationSupplier) =
        plugin(dependency.asProvider())

    compileOnly(gradleKotlinDsl())
    compileOnly(plugin(libs.plugins.kotlin.jvm))

    implementation(libs.javapoet)
    implementation(libs.kotlinpoet)

    testImplementation(gradleTestKit())
    testImplementation(gradleKotlinDsl())
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
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
    doLast { Thread.sleep(2000) } // allows GradleRunner to store JaCoCo data before computing task outputs
}

tasks.check {
    dependsOn("jacocoTestReport")
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
