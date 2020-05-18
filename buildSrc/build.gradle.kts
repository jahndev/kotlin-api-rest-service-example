plugins {
    `kotlin-dsl`
}

val artifactoryUser: String? by project
val artifactoryKey: String? by project

repositories {
    maven {
        credentials {
            username = System.getenv("ARTIFACTORY_USERNAME") ?: artifactoryUser
            password = System.getenv("ARTIFACTORY_PASSWORD") ?: artifactoryKey
        }
        url = uri("https://artifactory.example.repo/artifactory/shared-services-maven")
    }
    jcenter()
    google()
    mavenCentral()
}
