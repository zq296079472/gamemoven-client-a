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
        maven {
            url = uri("https://artifacts.applovin.com/android")
        }
        // 本地 Maven 仓库（用于测试 game-sdk）
        maven {
            url = uri("file://${rootDir}/game-sdk/build/maven")
        }
    }
}

rootProject.name = "GameMoven"
include(":app")
include(":game-sdk")
include(":libservice")
// include(":libcocos")  // 如果有libcocos请取消注释
include(":client-a-sdk") 
 
