# What is this file

Rubiks file is python representation of your deployment config. Yaml deployment config and service definitions will be generated based on this file

## Dependencies
 * a local clone of [openshift-shared-services](https://git.example.com/infrastructure/shared-services/openshift-shared-services)
 * [Rubiks](https://github.com/example-global/rubiks) installed on your machine
 * [OC v1.5.1](https://github.com/openshift/origin/releases/tag/v1.5.1) CLI tool installed in your $PATH

## Prerequisite
 * login with your `oc` command to your openshift cluster with the command copied from {openshift-server-url}/console/command-line

## Create a new OpenShift app from the kotlin-api-rest-service-example.ekube file

 * If it doesn't exist, create a folder inside the `openshift-shared-services` repo in the `src/` directory with the 
 same name as the `service_namespace`.
 * Copy the `kotlin-api-rest-service-example.ekube` file to it and rename it appropriately.
 * Run `$ rubiks generate`.
 * Create the project in the OpenShift cluster.
 * `$ cd out/{cluster_name}/{service_namespace}/`
 * Use `$ rubiks order .` to get the order in which the files should be applied.
 * Run `$ oc create -f {file}.yaml` where `{file}` is the name of a file to apply.
 * Commit those files in `out/` and `src/` and push them.

## Remove an app from OpenShift
The following steps are necessary in order to remove an app from the OpenShift cluster:
 * In the `openshift-shared-services` repo run `$ cd out/{cluster_name}/{service_namespace}` where `{service_namespace}` is the
 namespace that contains the app to remove.
 * Run `$ oc delete -f service-{service_name}.yaml`.
 * Run `$ oc delete -f deploymentconfig-{service_name}.yaml`.
 * Run `$ oc delete -f secret-{service_name}.yaml`.
 * Remove the service ekube file with `$ rm src/{service_namespace}/kotlin-api-rest-service-example.ekube`.
 * Remove all OpenShift specific resource files in the `out/{cluster_name}/{service_namespace}` that are related to the
 previous `{service}.ekube` file. Usually, they contain `{service}` name in their name.
