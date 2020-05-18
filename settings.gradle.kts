pluginManagement {
    val artifactoryUser: String? by settings
    val artifactoryKey: String? by settings

    repositories {
        gradlePluginPortal()
        maven {
            credentials {
                username = System.getenv("ARTIFACTORY_USERNAME") ?: artifactoryUser
                password = System.getenv("ARTIFACTORY_PASSWORD") ?: artifactoryKey
            }
            url = uri("https://artifactory.example.repo/artifactory/shared-services-maven")
        }
    }
}

include("app")

with(rootProject) {
    name = "kotlin-api-rest-service-example"
    children.forEach { it.buildFileName = "${it.name}.gradle.kts" }
}

buildCache {
    local {
        isEnabled = !System.getenv().containsKey("CI")
    }
}
