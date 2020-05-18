import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("idea")
    id("org.springframework.boot") version Versions.org_springframework_boot
    id("io.spring.dependency-management") version Versions.io_spring_dependency_management_gradle_plugin
    id("org.jetbrains.kotlin.plugin.noarg") version Versions.org_jetbrains_kotlin_plugin_noarg_gradle_plugin
}

// -------------------------- Dependencies ------------------------------------

val newrelic by configurations.creating

dependencies {
    newrelic(Libs.newrelic_agent)
    api(Libs.spring_boot_starter_web) {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    api(Libs.httpclient)
    api(Libs.spring_boot_starter_security)
    api(Libs.spring_boot_starter_actuator)
    api(Libs.spring_boot_starter_aop)
    api(Libs.jackson_module_kotlin)
    api(Libs.logback_json_classic)
    api(Libs.logback_jackson)
    api(Libs.kotlin_logging)
    api(Libs.commons_codec)
    implementation(Libs.spring_boot_starter_cache)
    implementation(Libs.spring_retry)
    implementation(Libs.newrelic_api)
    implementation(Libs.spring_boot_starter_jetty)
    implementation(Libs.payment_client)
    implementation(Libs.authentication_client)
    implementation(Libs.hydra_client)
    implementation(Libs.payment_hateoas_api)
    implementation(Libs.javaJWT)
    testImplementation(Libs.spring_boot_starter_test)
    testImplementation(Libs.junit_platform_commons)
    testImplementation(Libs.mockk)
    testImplementation(Libs.springmockk)
}

// -------------------------- Building Application ----------------------------

val copyNewRelicAgent by tasks.creating(Copy::class) {
    from(newrelic)
    from("$projectDir/src/main/resources") {
        include("newrelic.yml")
    }
    into("$rootDir/docker/app/build/")
    rename {
        if (it == "newrelic-agent-${Versions.com_newrelic_agent_java}.jar") "newrelic.jar"
        else it
    }
}

val copyNecessaryFiles by tasks.creating(Copy::class) {
    from("$projectDir/src/main/resources") {
        include("application.yml")
    }
    into("$rootDir/docker/app/build")
}

tasks {
    getByName<BootJar>("bootJar") {
        baseName = rootProject.name
        classifier = ""
        destinationDir = file("$rootDir/docker/app/build/")
        dependsOn.add(copyNewRelicAgent)
        dependsOn.add(copyNecessaryFiles)
    }
}
