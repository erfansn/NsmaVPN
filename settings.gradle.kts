import java.net.URI

pluginManagement {
    includeBuild("build-logic")
    repositories {
        // Sometimes, it worked instead of google() notation
        // maven { url = uri("https://maven.google.com") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Sometimes, it worked instead of google() notation
        // maven { url = uri("https://maven.google.com") }
        google()
        mavenCentral()
        maven { url = URI.create("https://jitpack.io") }
    }
    versionCatalogs {
        create("sdk") {
            version("compile", "34")
            version("target", "34")
        }
        create("jvm") {
            version("toolchain", "17")
        }
    }
}

rootProject.name = "NsmaVPN"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":macrobenchmark")
