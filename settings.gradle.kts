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
        maven("https://jitpack.io")
    }
    versionCatalogs {
        create("sdk") {
            version("compile", "36")
            version("target", "36")
        }
        create("jvm") {
            version("toolchain", "21")
        }
    }
}

rootProject.name = "NsmaVPN"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":macrobenchmark")
