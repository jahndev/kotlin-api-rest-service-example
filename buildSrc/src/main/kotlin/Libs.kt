import kotlin.String

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Update this file with
 *   `$ ./gradlew buildSrcVersions` */
object Libs {
    /**
     * https://github.com/qos-ch/logback-contrib/wiki */
    const val logback_jackson: String = "ch.qos.logback.contrib:logback-jackson:" +
            Versions.ch_qos_logback_contrib

    /**
     * https://github.com/qos-ch/logback-contrib/wiki */
    const val logback_json_classic: String = "ch.qos.logback.contrib:logback-json-classic:" +
            Versions.ch_qos_logback_contrib

    /**
     * https://github.com/FasterXML/jackson-module-kotlin */
    const val jackson_module_kotlin: String =
            "com.fasterxml.jackson.module:jackson-module-kotlin:" + Versions.jackson_module_kotlin

    const val com_github_ben_manes_versions_gradle_plugin: String =
            "com.github.ben-manes.versions:com.github.ben-manes.versions.gradle.plugin:" +
            Versions.com_github_ben_manes_versions_gradle_plugin

    const val ktlint: String = "com.github.shyiko:ktlint:" + Versions.ktlint

    /**
     * https://newrelic.com/ */
    const val newrelic_agent: String = "com.newrelic.agent.java:newrelic-agent:" +
            Versions.com_newrelic_agent_java

    /**
     * https://newrelic.com/ */
    const val newrelic_api: String = "com.newrelic.agent.java:newrelic-api:" +
            Versions.com_newrelic_agent_java

    /**
     * https://github.com/Ninja-Squad/springmockk */
    const val springmockk: String = "com.ninja-squad:springmockk:" + Versions.springmockk

    const val authentication_client: String = "com.example.ps:authentication-client:" +
            Versions.com_example_ps

    const val hydra_client: String = "com.example.ps:hydra-client:" + Versions.com_example_ps

    const val payment_client: String = "com.example.ps:payment-client:" + Versions.com_example_ps

    const val payment_hateoas_api: String = "com.example.ps:payment-hateoas-api:" + Versions.com_example_ps

    /**
     * https://commons.apache.org/proper/commons-codec/ */
    const val commons_codec: String = "commons-codec:commons-codec:" + Versions.commons_codec

    const val de_fayard_buildsrcversions_gradle_plugin: String =
            "de.fayard.buildSrcVersions:de.fayard.buildSrcVersions.gradle.plugin:" +
            Versions.de_fayard_buildsrcversions_gradle_plugin

    /**
     * https://github.com/MicroUtils/kotlin-logging */
    const val kotlin_logging: String = "io.github.microutils:kotlin-logging:" +
            Versions.kotlin_logging

    const val detekt_cli: String = "io.gitlab.arturbosch.detekt:detekt-cli:" +
            Versions.io_gitlab_arturbosch_detekt

    const val io_gitlab_arturbosch_detekt_gradle_plugin: String =
            "io.gitlab.arturbosch.detekt:io.gitlab.arturbosch.detekt.gradle.plugin:" +
            Versions.io_gitlab_arturbosch_detekt

    /**
     * http://mockk.io */
    const val mockk: String = "io.mockk:mockk:" + Versions.mockk

    const val io_spring_dependency_management_gradle_plugin: String =
            "io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:" +
            Versions.io_spring_dependency_management_gradle_plugin

    /**
     * http://hc.apache.org/httpcomponents-client */
    const val httpclient: String = "org.apache.httpcomponents:httpclient:" + Versions.httpclient

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String =
            "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:" +
            Versions.org_jetbrains_kotlin_jvm_gradle_plugin

    const val org_jetbrains_kotlin_plugin_noarg_gradle_plugin: String =
            "org.jetbrains.kotlin.plugin.noarg:org.jetbrains.kotlin.plugin.noarg.gradle.plugin:" +
            Versions.org_jetbrains_kotlin_plugin_noarg_gradle_plugin

    const val org_jetbrains_kotlin_plugin_spring_gradle_plugin: String =
            "org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:" +
            Versions.org_jetbrains_kotlin_plugin_spring_gradle_plugin

    const val spring_retry = "org.springframework.retry:spring-retry"

    const val spring_boot_starter_aop = "org.springframework.boot:spring-boot-starter-aop:" +
            Versions.spring_boot_starter_aop

    /**
     * https://kotlinlang.org/ */
    const val kotlin_allopen: String = "org.jetbrains.kotlin:kotlin-allopen:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/ */
    const val kotlin_noarg: String = "org.jetbrains.kotlin:kotlin-noarg:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/ */
    const val kotlin_reflect: String = "org.jetbrains.kotlin:kotlin-reflect:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/ */
    const val kotlin_scripting_compiler_embeddable: String =
            "org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/ */
    const val kotlin_stdlib_jdk8: String = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://junit.org/junit5/ */
    const val junit_jupiter_api: String = "org.junit.jupiter:junit-jupiter-api:" +
            Versions.junit_jupiter_api

    /**
     * https://junit.org/junit5/ */
    const val junit_jupiter_engine: String = "org.junit.jupiter:junit-jupiter-engine:" +
            Versions.junit_jupiter_engine

    /**
     * https://junit.org/junit5/ */
    const val junit_jupiter_params: String = "org.junit.jupiter:junit-jupiter-params:" +
            Versions.junit_jupiter_params

    /**
     * https://junit.org/junit5/ */
    const val junit_platform_commons: String = "org.junit.platform:junit-platform-commons:" +
            Versions.org_junit_platform

    /**
     * https://junit.org/junit5/ */
    const val junit_platform_engine: String = "org.junit.platform:junit-platform-engine:" +
            Versions.org_junit_platform

    const val org_owasp_dependencycheck_gradle_plugin: String =
            "org.owasp.dependencycheck:org.owasp.dependencycheck.gradle.plugin:" +
            Versions.org_owasp_dependencycheck_gradle_plugin

    const val org_sonarqube_gradle_plugin: String = "org.sonarqube:org.sonarqube.gradle.plugin:" +
            Versions.org_sonarqube_gradle_plugin

    /**
     * https://projects.spring.io/spring-boot/#/spring-boot-parent/spring-boot-tools/org.springframework.boot.gradle.plugin
            */
    const val org_springframework_boot_gradle_plugin: String =
            "org.springframework.boot:org.springframework.boot.gradle.plugin:" +
            Versions.org_springframework_boot

    /**
     * https://projects.spring.io/spring-boot/#/spring-boot-parent/spring-boot-starters/spring-boot-starter-actuator
            */
    const val spring_boot_starter_actuator: String =
            "org.springframework.boot:spring-boot-starter-actuator:" +
            Versions.org_springframework_boot

    /**
     * https://projects.spring.io/spring-boot/#/spring-boot-parent/spring-boot-starters/spring-boot-starter-jetty
            */
    const val spring_boot_starter_jetty: String =
            "org.springframework.boot:spring-boot-starter-jetty:" +
            Versions.org_springframework_boot

    /**
     * https://projects.spring.io/spring-boot/#/spring-boot-parent/spring-boot-starters/spring-boot-starter-security
            */
    const val spring_boot_starter_security: String =
            "org.springframework.boot:spring-boot-starter-security:" +
            Versions.org_springframework_boot

    /**
     * https://projects.spring.io/spring-boot/#/spring-boot-parent/spring-boot-starters/spring-boot-starter-test
            */
    const val spring_boot_starter_test: String =
            "org.springframework.boot:spring-boot-starter-test:" + Versions.org_springframework_boot

    /**
     * https://projects.spring.io/spring-boot/#/spring-boot-parent/spring-boot-starters/spring-boot-starter-web
            */
    const val spring_boot_starter_web: String =
            "org.springframework.boot:spring-boot-starter-web:" + Versions.org_springframework_boot

    const val javaJWT = "com.auth0:java-jwt:${Versions.javaJWT}"

    const val spring_boot_starter_cache: String =
        "org.springframework.boot:spring-boot-starter-cache:" + Versions.org_springframework_starter_cache

}
