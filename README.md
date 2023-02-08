# Clusterless

A command line tool for deploying continuously processing workloads for data that is continuously arriving, in the
cloud.

By standardizing the metadata as new data arrives, workloads can be implemented behind a consistent interface, and can
be easily event-driven by upstream availability.

Not only can heterogeneous workloads can be chained together, they can be

- back-filled - after deployment (of new dataset version)
- replayed - to correct for changes
- checked for gaps - due to upstream failures
- enabled/disabled - trivially pause or restart event listening

Where workloads can be:

- repartitioning data for improved accessibility (group the data by new partition keys)
- feature extraction
- training and validation
- GDPR support via tokenization or retention enforcement

And can be implemented as:

- Docker images (Python, Java, Node, etc)
- Serverless functions (Python, Java, Node, etc)
- Native services (AWS SageMaker, AWS Athena, AWS Glue, etc)

Currently supported cloud substrates:

- AWS

See the [docs](docs) folder for wip documentation.

## Prerequisites

Install [nvm](https://github.com/nvm-sh/nvm):

> curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash

Install node/npm:

> nvm install v19.4.0

Install the AWD CDK:

> npm install -g aws-cdk

Install Java 19 via [sdkman](https://sdkman.io):

> sdk install java 22.3.r19-grl

## Building

To build the command line tools:

> ./gradlew installDist

This will create the root main CLI interface and any substrate interfaces, [see below](#optional) for the paths.

Currently available are:

- `cls`
- `cls-aws`

## Running

> cls --help

## Optional

Use [direnv](https://direnv.net) to simplify managing paths:

Add to `.envrc`:

```shell
PATH_add ~/some_dir/clusterless/clusterless-main/build/install/cls/bin
PATH_add ~/some_dir/clusterless/clusterless-substrate-aws-kernel/build/install/awsKernel/bin
```

