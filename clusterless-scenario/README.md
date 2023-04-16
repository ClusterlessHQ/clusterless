# Scenarios

This project provides a tool for running long-lived testing scenarios.

A scenario follows this simple pattern:

- deploy a project
- push data so the ingress boundary can handle it
- verify data is arriving in the tail of the pipeline
- destroy the project

## Running

### Gradle

> ./gradlew clean :clusterless-substrate-aws-scenario:run

### Console

> ./gradlew clean :clusterless-substrate-aws-scenario:installDist
> ./clusterless-substrate-aws-scenario/build/install/cls-scenario/bin/cls-scenario --help
