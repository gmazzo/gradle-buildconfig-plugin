import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm")
}

apply(from = "../buildShared.gradle.kts")

base.archivesBaseName = "gradle-buildconfig-plugin"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:javapoet:1.11.1")
    implementation("com.squareup:kotlinpoet:1.0.1")
}

gradlePlugin {
    plugins {
        create("buildconfig") {
            id = "com.github.gmazzo.buildconfig"
            implementationClass = "com.github.gmazzo.gradle.plugins.BuildConfigPlugin"
        }
    }
}

tasks {
    withType(KotlinCompile::class).all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
