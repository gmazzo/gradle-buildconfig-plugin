plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.axion.release)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.publicationsReport)
    alias(libs.plugins.jacoco.testkit)
}

group = "com.github.gmazzo.buildconfig"
description =
    "A plugin for generating BuildConstants for any kind of Gradle projects: Java, Kotlin, Groovy, etc. Designed for KTS scripts."
version = scmVersion.version

java.toolchain.languageVersion = JavaLanguageVersion.of(8)
kotlin.compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>) =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

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

val originUrl = providers
    .exec { commandLine("git", "remote", "get-url", "origin") }
    .standardOutput.asText.map { it.trim() }

gradlePlugin {
    vcsUrl = originUrl
    website = originUrl

    plugins {
        create("buildconfig") {
            id = "com.github.gmazzo.buildconfig"
            displayName = name
            implementationClass = "com.github.gmazzo.buildconfig.BuildConfigPlugin"
            description = project.description
            tags.addAll("buildconfig", "java", "kotlin", "kotlin-multiplatform")
        }
    }
}

mavenPublishing {
    publishToMavenCentral("CENTRAL_PORTAL", automaticRelease = true)

    pom {
        name = "${rootProject.name}-${project.name}"
        description = provider { project.description }
        url = originUrl

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit/"
            }
        }

        developers {
            developer {
                id = "gmazzo"
                name = id
                email = "gmazzo65@gmail.com"
            }
        }

        scm {
            connection = originUrl
            developerConnection = originUrl
            url = originUrl
        }
    }
}

tasks.withType<Test> {
    workingDir = temporaryDir
    useJUnitPlatform()
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports.xml.required = true
}

afterEvaluate {
    tasks.named<Jar>("javadocJar") {
        from(tasks.dokkaGeneratePublicationJavadoc)
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    mustRunAfter(tasks.publishPlugins)
}

tasks.publishPlugins {
    enabled = "$version".matches("\\d+(\\.\\d+)+".toRegex())
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}
