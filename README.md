# kotlin-api-rest-service-example

[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

## Description
local methods for colombia bank transfer payments

`[!]` Before starting working, you should definitely read our guides:

- [Kotlin Code Style](codestyle.md)
- [How to sign commits](https://example.atlassian.net/wiki/spaces/PS/pages/352059703/Signing+Commits)

## Documentation
Links to any documentation for the service including any machine readable definitions like an OpenAPI definition or
Postman Collection, as well as any human readable documentation generated from definitions, or hand crafted and
published as part of the repository.

## Requirements
An outline of what other services, tooling, and libraries needed to make a service operate, providing a complete list
of EVERYTHING required to work properly.

### IntelliJ

To import the project code style, go to `Preferences`->`Editor`->`Code Style`->`Kotlin`->`Scheme`->`Import Scheme`
-> `Intellij IDEA code style XML` and select `ktlint.xml`

## Setup
A step by step outline from start to finish of what is needed to setup and operate a service, providing as much detail
as you possibly for any new user to be able to get up and running with a service.

### Service Deployment

In [.gitlab-ci.yml](.gitlab-ci.yml), the template `.deployment_template` can be used to set the deployments job for
staging and production environments. 

The variables that each individual job needs to set are:
- `OC_TOKEN` and `OC_URL` with the token and the url for OpenShift.
- `NR_APP_NAME`, `NR_API_KEY`, `NR_API_ID` and `NR_ACCOUNT_ID` with the name, the api key, the app id and the account id
for New Relic.
- `NR_INSIGHTS_API_KEY` with the api key for the New Relic Insights.

The global variables that need to be set are:
- `OC_PROJECT` and `OC_APP` with the name of the project and of the deployment config in OpenShift.

### Dependencies setup ...

#### ... Java

To easily manage different `java` versions, [sdkman](http://sdkman.io/) is the suggested tool. Once installed, run
the following commands

    $ sdk list java
    $ sdk install java 11.x.x-zulu

Where `11.x.x-zulu` is the latest available version of OpenJDK 11 on sdkman.

Then, save the following environment variables in your profile:
- `JAVA_HOME` to `$HOME/.sdkman/candidates/java/current`
- `PATH` to `$JAVA_HOME/bin:$PATH`

#### ... Artifactory

- Login to [artifactory.example.repo](https://artifactory.example.repo/) and create an API KEY from the
User Profile.
- Run `$ mkdir $HOME/.gradle` if gradle is not setup for your machine.
- Create/update the file `$HOME/.gradle/gradle.properties` adding the properties:

```    
artifactoryUser=<ArtifactoryUsername>
artifactoryKey=<API KEY Generated>
```

- Set the environment variable `GRADLE_USER_HOME` to `$HOME/.gradle`.
- Run `$ ./gradlew shadowJar` to verify that everything compiles and works.

#### ... updating the dependencies' versions

The Gradle plugin [buildSrcVersions](https://github.com/jmfayard/buildSrcVersions) is used to check the dependencies,
create the [Libs](buildSrc/src/main/kotlin/Libs.kt) and [Versions](buildSrc/src/main/kotlin/Versions.kt) files and
comment about new versions. The version currently used contains a bug that is already fixed and will be available in versions
0.4.0, and that requires to set all versions with value `"none"` to `""`. To run the plugin, use the command 
`$ .gradlew buildSrcVersions`.

### How to run the application locally

```    
MainClass: com.example.catalog.CatalogServiceApplication
Environment variables: spring.profiles.active=local
```

### Kubernetes deployment

Follow the instructions in the [kubernetes-readme](rubiks/README.md) on how to deploy the application in the necessary
clusters.

### Badges
If you have `master` access to Gitlab, you can add badges by selecting `Settings`->`Badges`. The following badges should
be added to the repository.

1. **Pipeline**
    - Link: `https://git.example.com/%{project_path}/pipelines`
    - Badge image URL: `https://git.example.com/%{project_path}/badges/%{default_branch}/pipeline.svg`

2. **Coverage**
    - Link: `https://git.example.com/%{project_path}/commits/%{default_branch}`
    - Badge image URL: `https://git.example.com/%{project_path}/badges/%{default_branch}/coverage.svg`

### Scheduled Jobs

You can setup [Scheduled Pipelines](https://docs.gitlab.com/ee/user/project/pipelines/schedules.html) by going to `CI/CD`
->`Schedules`->`New schedule`. The template supports the following jobs to be scheduled:

1. **Vulnerabilities Security Check**
    - Set a scheduled pipeline with the variable `SECURITY`.

2. **Dependencies Check**
    - Set a scheduled pipeline with the variable `DEPENDENCIES`.
    - Set a project/group variable `SLACK_DEPENDENCIES_URL` with the url of a slack channel webhook.

## Testing
Providing details and instructions for mocking, monitoring, and testing a service, including any services or tools used,
as well as links or reports that are part of active testing for a service.

## Configuration
An outline of all configuration and environmental variables that can be adjusted or customized as part of service
operations, including as much detail on default values, or options that would produce different known results for a
service.

OpenShift configurations are available in the [OpenShift folder](openshift) and are divided by cluster in which they 
are deployed. To add/update/remove them, change the `*.yaml` file, commit and push the files and create a new pipeline 
with the environment variable `CONFIG` set to `true`. 

## Discussion
/

## Owner
The name, title, email, phone, or other relevant contact information for the owner, or owners of a service providing
anyone with the information they need to reach out to person who is responsible for a service.
