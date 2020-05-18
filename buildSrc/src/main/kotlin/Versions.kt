import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val ch_qos_logback_contrib: String = "0.1.5" 

    const val jackson_module_kotlin: String = "2.10.1" 

    const val com_github_ben_manes_versions_gradle_plugin: String = "0.20.0" // available: "0.27.0"

    const val ktlint: String = "0.31.0" // available: "0.36.0"

    const val com_newrelic_agent_java: String = "5.0.0" // available: "5.8.0"

    const val springmockk: String = "1.1.2" // available: "1.1.3"

    const val com_example_ps: String = "0.4.4" // available: "0.4.7"

    const val commons_codec: String = "1.12" // available: "1.13"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" // available: "0.7.0"

    const val kotlin_logging: String = "1.6.26" // available: "1.7.8"

    const val io_gitlab_arturbosch_detekt: String = "1.0.0.RC9.2" 

    const val mockk: String = "1.9.3" 

    const val io_spring_dependency_management_gradle_plugin: String = "1.0.7.RELEASE" 
            // available: "1.0.8.RELEASE"

    const val httpclient: String = "4.5.8" // available: "4.5.10"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.31" // available: "1.3.61"

    const val org_jetbrains_kotlin_plugin_noarg_gradle_plugin: String = "1.3.31" 
            // available: "1.3.61"

    const val org_jetbrains_kotlin_plugin_spring_gradle_plugin: String = "1.3.31" 
            // available: "1.3.61"

    const val org_jetbrains_kotlin: String = "1.3.31" // available: "1.3.61"

    const val junit_jupiter_api: String = "5.4.1" // available: "5.5.2"

    const val junit_jupiter_engine: String = "5.4.2" // available: "5.5.2"

    const val junit_jupiter_params: String = "5.4.1" // available: "5.5.2"

    const val org_junit_platform: String = "1.4.2" // available: "1.5.2"

    const val org_owasp_dependencycheck_gradle_plugin: String = "5.0.0" // available: "5.2.4"

    const val org_sonarqube_gradle_plugin: String = "2.6.2" // available: "2.8"

    const val org_springframework_boot: String = "2.2.5.RELEASE"

    const val org_springframework_starter_cache: String = "2.2.5.RELEASE"

    const val javaJWT = "3.8.3"

    const val spring_boot_starter_aop: String = "2.2.5.RELEASE"
    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "4.10.3"

        const val currentVersion: String = "6.0.1"

        const val nightlyVersion: String = "6.1-20191203063738+0000"

        const val releaseCandidate: String = ""
    }
}
