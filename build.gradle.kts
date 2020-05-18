import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator

plugins {
    jacoco
    `java-library`
    id("com.github.ben-manes.versions") version Versions.com_github_ben_manes_versions_gradle_plugin
    id("io.gitlab.arturbosch.detekt") version Versions.io_gitlab_arturbosch_detekt
    id("org.jetbrains.kotlin.jvm") version Versions.org_jetbrains_kotlin_jvm_gradle_plugin
    id("org.jetbrains.kotlin.plugin.spring") version Versions.org_jetbrains_kotlin_plugin_spring_gradle_plugin
    id("org.owasp.dependencycheck") version Versions.org_owasp_dependencycheck_gradle_plugin
    id("org.sonarqube") version Versions.org_sonarqube_gradle_plugin
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
}

allprojects {
    val artifactoryUser: String? by rootProject
    val artifactoryKey: String? by rootProject

    repositories {
        maven {
            credentials {
                username = System.getenv("ARTIFACTORY_USERNAME") ?: artifactoryUser
                password = System.getenv("ARTIFACTORY_PASSWORD") ?: artifactoryKey
            }
            url = uri("https://artifactory.example.repo/artifactory/shared-services-maven")
        }

        maven {
            credentials {
                username = System.getenv("ARTIFACTORY_USERNAME") ?: artifactoryUser
                password = System.getenv("ARTIFACTORY_PASSWORD") ?: artifactoryKey
            }
            url = uri("https://artifactory.example.repo/artifactory/nc-exampleber-payments-maven-release")
        }

        jcenter()
        google()
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("com.github.ben-manes.versions")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("jacoco")
        plugin("java-library")
        plugin("kotlin")
        plugin("org.jetbrains.kotlin.plugin.spring")
    }

    // -------------------------- Dependencies ------------------------------------

    val ktlint by configurations.creating

    dependencies {
        api(Libs.kotlin_reflect)
        api(Libs.kotlin_stdlib_jdk8)
        ktlint(Libs.ktlint)
        testImplementation(Libs.junit_jupiter_engine)
        testRuntimeClasspath(Libs.junit_platform_engine)
        testImplementation(Libs.junit_jupiter_api)
        testImplementation(Libs.junit_jupiter_params)
    }

    tasks {
        //updates all the dependencies in Libraries
        getByName<DependencyUpdatesTask>("dependencyUpdates") {
            resolutionStrategy {
                componentSelection {
                    all {
                        val rejected = listOf("alpha", "beta", "rc", "cr", "m", "dmr")
                            .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                            .any { it.matches(candidate.version) }
                        if (rejected) {
                            reject("Release candidate")
                        }
                    }
                }
            }
        }
        //defines how to compile kotlin code
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        //defines how test should be run and how restuls should be reported
        withType<Test> {
            useJUnitPlatform()
            reports.html.destination = file("${reporting.baseDir}/$name")
            testLogging { exceptionFormat = TestExceptionFormat.FULL }
            // https://github.com/gradle/gradle/issues/5431
            addTestListener(object : TestListener {
                override fun beforeTest(testDescriptor: TestDescriptor) {}
                override fun beforeSuite(suite: TestDescriptor) {}
                override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
                override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                    if (suite.parent == null) {
                        println(buildString {
                            append("Results: ${result.resultType} (${result.testCount} tests, ")
                            append("${result.successfulTestCount} successes, ")
                            append("${result.failedTestCount} failures, ")
                            append("${result.skippedTestCount} skipped)")
                        })
                    }
                }
            })
        }

        val ktlintMain = "com.github.shyiko.ktlint.Main"
        val ktlintArgs = listOf("-F", "$projectDir/src/**/*.kt")
        create<JavaExec>("ktlintCheck") {
            description = "Runs ktlint on all kotlin sources in this project."
            //put our ktlintCheck under the same group as check
            tasks["check"].group?.let { group = it }
            main = ktlintMain
            classpath = ktlint
            args = ktlintArgs.drop(1)
        }
        create<JavaExec>("ktlintFormat") {
            description = "Runs the ktlint formatter on all kotlin sources in this project."
            group = "Formatting tasks"
            main = ktlintMain
            classpath = ktlint
            args = ktlintArgs
        }
    }

    detekt {
        toolVersion = Versions.io_gitlab_arturbosch_detekt
        input = files("$projectDir/src/main/kotlin", "$projectDir/src/test/kotlin")
        config = files("$rootDir/detekt.yml")
    }

    jacoco {
        toolVersion = "0.8.4"
    }
}

// -------------------------- Global Setup -----------------------------------

tasks {
    create<JacocoReport>("jacocoRootTestReport") {
        classDirectories = files("${project(":app").buildDir}/classes/kotlin/main")
        sourceDirectories = files("${project(":app").projectDir}/src/kotlin/main")

        val jacocoTestFiles = subprojects.map {
            val coverageFileLocation = "${it.buildDir}/jacoco/test.exec"
            if (file(coverageFileLocation).exists()) coverageFileLocation
            else ""
        }.filter { it.isNotEmpty() }.toTypedArray()

        logger.info("Aggregating next JaCoCo Coverage files: {}", jacocoTestFiles)
        executionData = files(*jacocoTestFiles)

        reports.csv.isEnabled = true
    }
}

configure<DependencyCheckExtension> {
    autoUpdate = true
    format = ReportGenerator.Format.ALL
}
