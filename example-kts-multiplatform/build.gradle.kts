import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet

plugins {
    kotlin("multiplatform") version "1.3.21"
    id("com.github.gmazzo.buildconfig") version "<latest>"
}

kotlin {
    jvm()
    js()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
            }
        }

        jvm() {
            compilations["main"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("stdlib-jdk8"))
                }
            }
            compilations["test"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("test-junit"))
                }
            }
        }

        js() {
            compilations["main"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("stdlib-js"))
                }
            }
            compilations["test"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("test-js"))
                }
            }
        }
    }
}

buildConfig {
    buildConfigField("String", "COMMON_VALUE", "\"aCommonValue\"")

    sourceSets.named<BuildConfigSourceSet>("jvmMain") {
        buildConfigField("String", "JVM_VALUE", "\"aJvmValue\"")
    }

    sourceSets.named<BuildConfigSourceSet>("jsMain") {
        buildConfigField("String", "JS_VALUE", "\"aJsValue\"")
    }
}
