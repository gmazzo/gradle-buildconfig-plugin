plugins {
    `kotlin-dsl` // TODO kotlin("jvm")
    `java-gradle-plugin`
    jacoco
}

java.toolchain.languageVersion = JavaLanguageVersion.of(8)

gradlePlugin {
    plugins {
        register("jacoco-gradle-testkit") {
            id = "io.github.gmazzo.gradle.testkit.jacoco"
            implementationClass = "io.github.gmazzo.gradle.testkit.jacoco.JacocoGradleTestKitPlugin"
        }
    }
}

dependencies {
    compileOnly("org.jacoco:org.jacoco.agent:${jacoco.toolVersion}:runtime")
}
