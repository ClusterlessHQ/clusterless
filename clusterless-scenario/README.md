# Scenarios

This project provides a tool for running long-lived testing scenarios.

A scenario follows this simple pattern:

- find all projects
- bootstrap all declared placements
- in parallel
  - deploy a project
  - push data so the ingress boundary can handle it
  - verify data is arriving in the tail of the pipeline
  - destroy the project
- destroy all bootstrapped resources

## Running

### Gradle

> ./gradlew clean scenarios

### Console

> ./gradlew clean :clusterless-scenario:installDist
> ./clusterless-scenario/build/install/cls-scenario/bin/cls-scenario --help
