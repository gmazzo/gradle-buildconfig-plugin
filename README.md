# gradle-buildconfig-plugin
A plugin for generating BuildConstants for any kind of Gradle projects: Java, Kotlin, Groovy, etc. Designed for KTS scripts.
Check [https://plugins.gradle.org/plugin/com.github.gmazzo.buildconfig](https://plugins.gradle.org/plugin/com.github.gmazzo.buildconfig)

[![Build Status](https://travis-ci.com/gmazzo/gradle-buildconfig-plugin.svg?branch=master)](https://travis-ci.com/gmazzo/gradle-buildconfig-plugin)
[![codecov](https://codecov.io/gh/gmazzo/gradle-buildconfig-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/gmazzo/gradle-buildconfig-plugin)

## Usage in KTS
On your `build.gradle.kts` add:
```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.11"
    id("com.github.gmazzo.buildconfig") version "0.1"
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
@file:Generated("com.github.gmazzo.gradle.plugins.tasks.BuildConfigKoltinGenerator")

const val APP_NAME: String = "example-kts"

const val APP_SECRET: String = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu"

const val BUILD_TIME: Long = 1550997069821L

const val FEATURE_ENABLED: Boolean = true

val MAGIC_NUMBERS: IntArray = intArrayOf(1, 2, 3, 4)

val MY_DATA: SomeData = SomeData("a",1)
```

## Usage in Groovy
On your `build.gradle` add:
```groovy
plugins {
    id 'java'
    id 'com.github.gmazzo.buildconfig' version '0.1'
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
@Generated("com.github.gmazzo.gradle.plugins.tasks.BuildConfigJavaGenerator")
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
    language("java")        // forces the language. Defaults to 'kotlin' if Kotlin's plugin was applied, 'java' otherwise
}
```
Will generate `com.foo.MyConfig` in a `MyConfig.java` file.

## Advanced
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
task("generateResourcesConstants") {
    doFirst {
        val resources = sourceSets["main"].resources

        resources.files.forEach {
            val name = it.name.toUpperCase().replace("\\W".toRegex(), "_")
            val path = it.relativeTo(resources.srcDirs.iterator().next())

            buildConfig.buildConfigField("java.io.File", "RESOURCE_$name", "File(\"$path\")")
        }
    }

    tasks["generateBuildConfig"].dependsOn(this)
}
```
Will generate in `BuildConfig.kt`:
```kotlin
val RESOURCE_CONFIG_LOCAL_PROPERTIES: File = File("config/local.properties")

val RESOURCE_CONFIG_PROD_PROPERTIES: File = File("config/prod.properties")

val RESOURCE_FILE1_JSON: File = File("file1.json")

val RESOURCE_FILE2_JSON: File = File("file2.json")
```
