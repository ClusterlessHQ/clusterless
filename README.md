# Clusterless

## Status

This project is under active development and considered alpha if not pre-alpha.

Please do play around with this project in order to provide early feedback, but do expect things to change until we hit
1.0 release.

See the [ROADMAP](ROADMAP.md) for planned capabilities.

## About

Clusterless is a tool for deploying scalable and secure data-processing workloads for continuously arriving data, across
clouds.

By standardizing the metadata as new data arrives, workloads can be implemented behind a consistent interface, and can
be easily deployed to listen for upstream availability events and fire new events to downstream listeners.

Not only can heterogeneous workloads can be chained together via events, they can be:

- back-filled - a new workload (or version) can run against historical data
- replayed - a fixed workload can re-run over a range of data to correct for an error in place
- checked for gaps - missing data can be accounted for by walking the upstream dependencies
- enabled/disabled - trivially pause or restart event listening
- versioned - new versions of workloads can be deployed without interfering with existing production pipelines
- tested - either locally before deployment, or using our [scenario tool](clusterless-scenario/README.md), as part of a
  full pipeline

Where workloads:

- reformat data (say from text to parquet)
- repartition data for improved accessibility and performance (group the data by new partition keys)
- perform feature extraction via custom code
- execute training and validation (via custom Python or simply calling SageMaker)
- enforce GDPR (privacy) compliance (via identity tokenization and retention)

And can be implemented as:

- Docker images (Python, Java, Node, etc)
- Serverless functions (Python, Java, Node, etc)
- Native services (AWS SageMaker, AWS Athena, AWS Glue, etc)

Where the intent isn't to have functional parity across substrates, but to ease secure and reliable interoperability
between them.

By leveraging native pay-as-you-go primitives, no runtimes or dedicated services need to be managed.

Zero data arriving means zero costs (other than storage for historical data).

Currently supported cloud substrates:

- AWS

See the [docs](docs) folder for wip documentation.

## Prerequisites

_If you already have Node and npm, skip to the AWS CDK install._

Install [nvm](https://github.com/nvm-sh/nvm):

> curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash

Install node/npm:

> nvm install v18.14.2

Install the AWD CDK:

> npm install -g aws-cdk@2.80.0

Install Java 19 via [sdkman](https://sdkman.io):

> sdk install java 22.3.r11-grl

## Building

To build the command line tools:

> ./gradlew installDist

This will create the root main CLI interface and any substrate interfaces, [see below](#optional) for the paths.

Currently available are:

- `cls` - the root cli interface
- `cls-aws` - the AWS interface only used as an escape hatch and testing

## Running

> cls --help

## Optional

Use [direnv](https://direnv.net) to simplify managing paths:

Add to `.envrc`:

```shell
use sdk java 22.3.r11-grl
PATH_add ~/some_dir/clusterless/clusterless-main/build/install/cls/bin
```
