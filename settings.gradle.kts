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
}

rootProject.name = "AnamWallet"
include(":app")

// Core modules
include(":core:common")
include(":core:ui")
include(":core:data")
include(":core:security")

// Feature modules  
include(":feature:main")
include(":feature:hub")
include(":feature:browser")
include(":feature:identity")
include(":feature:settings")
include(":feature:miniapp")
include(":feature:auth")
 