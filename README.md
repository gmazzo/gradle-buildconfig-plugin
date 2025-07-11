![GitHub](https://img.shields.io/github/license/gmazzo/gradle-buildconfig-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.gmazzo.buildconfig/com.github.gmazzo.buildconfig.gradle.plugin)](https://central.sonatype.com/artifact/com.github.gmazzo.buildconfig/com.github.gmazzo.buildconfig.gradle.plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.github.gmazzo.buildconfig)](https://plugins.gradle.org/plugin/com.github.gmazzo.buildconfig)
[![Build Status](https://github.com/gmazzo/gradle-buildconfig-plugin/actions/workflows/ci-cd.yaml/badge.svg)](https://github.com/gmazzo/gradle-buildconfig-plugin/actions/workflows/ci-cd.yaml)
[![codecov](https://codecov.io/gh/gmazzo/gradle-buildconfig-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/gmazzo/gradle-buildconfig-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:com.github.gmazzo.buildconfig+-repo:github.com/gmazzo/gradle-buildconfig-plugin)

[![Contributors](https://contrib.rocks/image?repo=gmazzo/gradle-buildconfig-plugin)](https://github.com/gmazzo/gradle-buildconfig-plugin/graphs/contributors)

# gradle-buildconfig-plugin

A plugin for generating BuildConstants for any kind of Gradle projects: Java, Kotlin, Android, Groovy, etc.
Designed for KTS scripts, with *experimental* support for Kotlin's **multi-platform** plugin

## Usage in KTS

On your `build.gradle.kts` add:

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "<kotlin version>"
    id("com.github.gmazzo.buildconfig") version "<current version>"
}

buildConfig {
    buildConfigField("APP_NAME", project.name)
    buildConfigField("APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
    buildConfigField<String>("OPTIONAL", null)
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
    buildConfigField("FEATURE_ENABLED", true)
    buildConfigField("MAGIC_NUMBERS", intArrayOf(1, 2, 3, 4))
    buildConfigField("STRING_LIST", arrayOf("a", "b", "c"))
    buildConfigField("MAP", mapOf("a" to 1, "b" to 2))
    buildConfigField("FILE", File("aFile"))
    buildConfigField("URI", uri("https://example.io"))
    buildConfigField("com.github.gmazzo.buildconfig.demos.kts.SomeData", "DATA", "SomeData(\"a\", 1)")

}
```

Will generate `BuildConfig.kt`:

```kotlin
internal object BuildConfig {
    internal const val APP_NAME: String = "kts"
    internal const val APP_VERSION: String = "\"0.1.0-demo\""
    internal const val APP_SECRET: String = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu"
    internal val OPTIONAL: String? = null
    internal const val BUILD_TIME: Long = 1_702_559_872_137L
    internal const val FEATURE_ENABLED: Boolean = true
    internal val MAGIC_NUMBERS: IntArray = intArrayOf(1, 2, 3)
    internal val STRING_LIST: Array<String> = arrayOf("a", "b", "c")
    internal val MAP: Map<String, Int> = mapOf("a" to 1, "b" to 2)
    internal val FILE: File = java.io.File("aFile")
    internal val URI: URI = java.net.URI.create("https://example.io")
    internal val DATA: SomeData = SomeData("a", 1)
}
```

> [!IMPORTANT]
> Avoid generating `File` entries with `Project.file` API, as they are created with absolute paths, and it will produce
> cache misses.
> ```kotlin
> buildConfigField("FILE", file("aFile")) // will create a file targeting `/your/project/root/aFile` -> DON'T!
> buildConfigField("FILE", file("aFile").relativeToOrSelf(projectDir)) // use this instead, for instance
> ```

## Usage in Groovy

On your `build.gradle` add:

```groovy
plugins {
    id 'java'
    id 'com.github.gmazzo.buildconfig' version < current version >
}

buildConfig {
    buildConfigField(String, 'APP_NAME', project.name)
    buildConfigField(String, "APP_VERSION", provider { project.version })
    buildConfigField(String, 'APP_SECRET', "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
    buildConfigField(String, 'OPTIONAL', null)
    buildConfigField(long, 'BUILD_TIME', System.currentTimeMillis())
    buildConfigField(boolean, 'FEATURE_ENABLED', true)
    buildConfigField(int[], "MAGIC_NUMBERS", [1, 2, 3])
    buildConfigField('List<String>', "STRING_LIST", ["a", "b", "c"])
    buildConfigField(Map.class, "MAP", [a: 1, b: 2])
    buildConfigField(File.class, "FILE", new File("aFile"))
    buildConfigField(URI.class, "URI", uri("https://example.io"))
    buildConfigField("com.github.gmazzo.buildconfig.demos.groovy.SomeData", "DATA", "new SomeData(\"a\", 1)")
}
```

Will generate `BuildConfig.java`:

```java
final class BuildConfig {
    public static final String APP_NAME = "groovy";
    public static final String APP_VERSION = "0.1.0-demo";
    public static final String APP_SECRET = "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu";
    public static final String OPTIONAL = null;
    public static final long BUILD_TIME = 1702559872111L;
    public static final boolean FEATURE_ENABLED = true;
    public static final int[] MAGIC_NUMBERS = {1, 2, 3};
    public static final String[] STRING_LIST = {"a", "b", "c"};
    public static final Map<String, Integer> MAP = java.util.Map.of("a", 1, "b", 2);
    public static final File FILE = new java.io.File("aFile");
    public static final URI URI = java.net.URI.create("https://example.io");
    public static final SomeData DATA = new SomeData("a", 1);
}
```

## Customizing the class

If you add in your `build.gradle.kts`:

```kotlin
buildConfig {
    className("MyConfig")   // forces the class name. Defaults to 'BuildConfig'
    packageName("com.foo")  // forces the package. Defaults to '${project.group}'

    useJavaOutput()                                 // forces the outputType to 'java'
    useKotlinOutput()                               // forces the outputType to 'kotlin', generating an `object`
    useKotlinOutput {
        topLevelConstants = true
    }    // forces the outputType to 'kotlin', generating top-level declarations
    useKotlinOutput { internalVisibility = false }  // makes `BuildConfig` class `public` (defaults to `internal`)
}
```

Will generate `com.foo.MyConfig` in a `MyConfig.java` file.

## Secondary classes

Sometimes one generated does not fits your needs or code style.
You may add multiple classes with the following syntax:

```kotlin
buildConfig {
    buildConfigField("FIELD1", "field1")

    forClass("OtherClass") {
        buildConfigField("FIELD2", "field2")
    }

    forClass(packageName = "", className = "RootConfig") {
        buildConfigField("FIELD3", "field3")
    }
}
```

Will generate the files:

- `com.github.gmazzo.BuildConfig`
- `com.github.gmazzo.OtherClass`
- `RootConfig` (in the root package)

## Generate top-level constants

On your `build.gradle.kts` add:

```kotlin
buildConfig {
    useKotlinOutput { topLevelConstants = true }

    buildConfigField("APP_NAME", project.name)
    buildConfigField("APP_VERSION", provider { project.version })
}
```

Will generate `BuildConfig.kt`:

```kotlin
const val APP_NAME: String = "example-kts"

const val APP_VERSION: String = "0.0.1"
```

## Added documentation (JavaDoc / KDoc) to the generated class

On your `build.gradle.kts` add:

```kotlin
buildConfig {
    documentation.set("Generated by BuildConfig plugin")
}
```

> [!NOTE]
> `documentation` applies independently for each generated class

## Do not generate classes at Gradle Sync

By default, all `BuildConfigTask`s will be run as part of the Gradle Sync phase, to improve the developer experiece by
having always an up-to-date version of the generated BuildConfig classes.

You can turn this behavior by setting the `com.github.gmazzo.buildconfig.generateAtSync` property to `false` in your
`gradle.properties` file or by using the extension DSL:

```kotlin
buildConfig {
    generateAtSync = false
}
```

## Advanced

### Generate constants for 'test' sourceSet (or any)

If you add in your `build.gradle.kts`:

```kotlin
buildConfig {
    sourceSets.getByName("test") {
        buildConfigField("TEST_CONSTANT", "aTestValue")
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
        buildConfig {
            buildConfigField(String, 'TEST_CONSTANT', 'aTestValue')
        }
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
buildConfig.forClass("BuildResources") {
  buildConfigField("A_CONSTANT", "aConstant")

  sourceSets["main"].resources.asFileTree.visit {
    if (!isDirectory) {
      val name = path.uppercase().replace("\\W".toRegex(), "_")

      buildConfigField(name, File(path))
    }
  }
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
def buildResources = buildConfig.forClass("BuildResources")
def generateResourcesConstants = tasks.register("generateResourcesConstants") {
    def resources = sourceSets["main"].resources.asFileTree

    inputs.files(resources)
    doFirst {
        resources.visit { file ->
            def name = file.path.toUpperCase().replaceAll("\\W", "_")

            buildResources.buildConfigField("java.io.File", name, "new File(\"$file.path\")")
        }
    }
}

tasks.generateBuildConfig {
    dependsOn(generateResourcesConstants)
}
```

Will generate in `BuildResources.java`:

```java
  public static final File CONFIG_LOCAL_PROPERTIES = new File("config/local.properties");

public static final File CONFIG_PROD_PROPERTIES = new File("config/prod.properties");

public static final File FILE2_JSON = new File("file2.json");

public static final File FILE1_JSON = new File("file1.json");
```
