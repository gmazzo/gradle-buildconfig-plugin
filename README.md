# gradle-buildconfig-plugin
A plugin for generating BuildConstants for any kind of Gradle projects: Java, Kotlin, Groovy, etc.
Designed for KTS scripts, with *experimental* support for Kotlin's **multi-platform** plugin

[![Plugins Site](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/gmazzo/gradle-buildconfig-plugin/maven-metadata.xml.svg?label=gradle-plugins)](https://plugins.gradle.org/plugin/com.github.gmazzo.buildconfig)
[![Build Status](https://travis-ci.com/gmazzo/gradle-buildconfig-plugin.svg?branch=master)](https://travis-ci.com/gmazzo/gradle-buildconfig-plugin)
[![codecov](https://codecov.io/gh/gmazzo/gradle-buildconfig-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/gmazzo/gradle-buildconfig-plugin)

## Usage in KTS
On your `build.gradle.kts` add:
```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.21"
    id("com.github.gmazzo.buildconfig") version <current version>
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_SECRET", "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    buildConfigField("boolean", "FEATURE_ENABLED", "${true}")
    buildConfigField("IntArray", "MAGIC_NUMBERS", "intArrayOf(1, 2, 3, 4)")
    buildConfigField("com.github.gmazzo.SomeData", "MY_DATA", "new SomeData(\"a\",1)")
}
```
Will generate `BuildConfig.kt`:
```kotlin
object BuildConfig {
    const val APP_NAME: String = "example-kts"

    const val APP_SECRET: String = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu"

    const val BUILD_TIME: Long = 1551108377126L

    const val FEATURE_ENABLED: Boolean = true

    val MAGIC_NUMBERS: IntArray = intArrayOf(1, 2, 3, 4)

    val MY_DATA: SomeData = SomeData("a",1)

    val RESOURCE_CONFIG_LOCAL_PROPERTIES: File = File("config/local.properties")

    val RESOURCE_CONFIG_PROD_PROPERTIES: File = File("config/prod.properties")

    val RESOURCE_FILE2_JSON: File = File("file2.json")

    val RESOURCE_FILE1_JSON: File = File("file1.json")
}
```

## Usage in Groovy
On your `build.gradle` add:
```groovy
plugins {
    id 'java'
    id 'com.github.gmazzo.buildconfig' version <current version>
}

buildConfig {
    buildConfigField('String', 'APP_NAME', "\"${project.name}\"")
    buildConfigField('String', 'APP_SECRET', "\"Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu\"")
    buildConfigField('long', 'BUILD_TIME', "${System.currentTimeMillis()}L")
    buildConfigField('boolean', 'FEATURE_ENABLED', "${true}")
    buildConfigField('int[]', 'MAGIC_NUMBERS', '{1, 2, 3, 4}')
    buildConfigField("com.github.gmazzo.SomeData", "MY_DATA", "new SomeData(\"a\",1)")
}
```
Will generate `BuildConfig.java`:
```java
public final class BuildConfig {
  public static final String APP_NAME = "example-groovy";

  public static final String APP_SECRET = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu";

  public static final long BUILD_TIME = 1550999393550L;

  public static final boolean FEATURE_ENABLED = true;

  public static final int[] MAGIC_NUMBERS = {1, 2, 3, 4};

  public static final SomeData MY_DATA = new SomeData("a",1);

  private BuildConfig() {
  }
}
```

## Customizing the class
If you add in your `build.gradle.kts`:
```kotlin
buildConfig {
    className("MyConfig")   // forces the class name. Defaults to 'BuildConfig'
    packageName("com.foo")  // forces the package. Defaults to '${project.group}'
    outputType("java")      // forces the outputType. Defaults to 'kotlin' if Kotlin's plugin was applied, 'java' otherwise
}
```
Will generate `com.foo.MyConfig` in a `MyConfig.java` file.

## Secondary classes
Sometimes one generated does not fits your needs or code style.
You may add multiple classes with the following syntax:
```kotlin
buildConfig {
    buildConfigField("String", "FIELD1", "\"field1\"")

    forClass("OtherClass") {
        buildConfigField("String", "FIELD2", "\"field2\"")
    }

    forClass(packageName = "", className = "RootConfig") {
        buildConfigField("String", "FIELD3", "\"field3\"")
    }
}
```
Will generate the files:
- `com.github.gmazzo.BuildConfig`
- `com.github.gmazzo.OtherClass`
- `RootConfig` (in the root package)

## Advanced
### Generate constants for 'test' sourceSet (or any)
If you add in your `build.gradle.kts`:
```kotlin
buildConfig {
    sourceSets.getByName("test") {
        buildConfigField("String", "TEST_CONSTANT", "\"aTestValue\"")
    }
}
```
Will generate in `TestBuildConfig.kt`:
```kotlin
object TestBuildConfig {
    const val TEST_CONSTANT: String = "aTestValue"
}
```
#### Or in Groovy:
```groovy
sourceSets {
    test {
        buildConfigField('String', 'TEST_CONSTANT', '"aTestValue"')
    }
}
```

### Generate constants from resources files
Assuming you have the following structure:
```
myproject
- src
  - main
    - resources
      - config
        - local.properties
        - prod.properties
      - file1.json
      - file1.json
```
If you add in your `build.gradle.kts`:
```kotlin
val generateBuildConfig by tasks

task("generateResourcesConstants") {
    val buildResources = buildConfig.forClass("BuildResources")

    doFirst {
        sourceSets["main"].resources.asFileTree.visit(Action<FileVisitDetails> {
            val name = path.toUpperCase().replace("\\W".toRegex(), "_")

            buildResources.buildConfigField("java.io.File", name, "File(\"$path\")")
        })
    }

    generateBuildConfig.dependsOn(this)
}
```
Will generate in `BuildResources.kt`:
```kotlin
object BuildResources {
    val CONFIG_LOCAL_PROPERTIES: File = File("config/local.properties")

    val CONFIG_PROD_PROPERTIES: File = File("config/prod.properties")

    val FILE1_JSON: File = File("file1.json")

    val FILE2_JSON: File = File("file2.json")
}
```
#### Or in Groovy:
```groovy
task("generateResourcesConstants") {
    def buildResources = buildConfig.forClass("BuildResources")

    doFirst {
        sourceSets["main"].resources.asFileTree.visit { file ->
            def name = file.path.toUpperCase().replaceAll("\\W", "_")

            buildResources.buildConfigField("java.io.File", name, "new File(\"$file.path\")")
        }
    }

    generateBuildConfig.dependsOn(it)
}
```
Will generate in `BuildResources.java`:
```java
  public static final File CONFIG_LOCAL_PROPERTIES = new File("config/local.properties");

  public static final File CONFIG_PROD_PROPERTIES = new File("config/prod.properties");

  public static final File FILE2_JSON = new File("file2.json");

  public static final File FILE1_JSON = new File("file1.json");
```
