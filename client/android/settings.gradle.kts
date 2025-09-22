@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    // Push-Client 용 Version Catalog 추가.
    versionCatalogs {
        create("push") {
            from(files("gradle/push-libs.versions.toml"))
        }
        create("client") {
            from(files("gradle/client-libs.versions.toml"))
        }
    }
}

rootProject.name = "android-sample"
include(
    ":app",
    ":modules:matrix-commons",
    ":modules:matrix-push-client"
)