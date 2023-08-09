# Clusterless

```text
                                       ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐                                
                                        Project A                                               
                                       │      _                │                                
                                             ╱ ╲      .─────.             ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ 
┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐     ╔══╬═══▶▕   ▏═══▶(|||||||)═╬═════╗     Project C           │
 Ingress Project                    ║        ╲ ╱      `─────'        ║    │    _                
│                             │     ║  │      ▔                │     ║        ╱ ╲      .─────. │
   ════▶╔ ═ ═ ╗                     ║      Workload                  ╚════╬═▶▕   ▏═══▶(|||||||) 
│          _                  │     ║  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘              ╲ ╱      `─────' │
        ║ ╱#╲ ║      .─────.        ║                                     │    ▔                
│  ════▶ ▕###▏ ════▶(|||||||)═╬═════╣                                          ▲               │
        ║ ╲#╱ ║      `─────'        ║                                     └ ─ ─║─ ─ ─ ─ ─ ─ ─ ─ 
│          ▔         Dataset  │     ║                                          ║                
   ════▶╚ ═ ═ ╝                     ║  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ╬ ─ ─ ─          
│      Boundary               │     ║         _                     _          ║      │         
 ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─      ║  │     ╱ ╲      .─────.      ╱ ╲      .─────.             
                                    ╚══════▶▕   ▏═══▶(|||||||)═══▶▕   ▏═══▶(|||||||)  │         
                                       │     ╲ ╱      `─────'      ╲ ╱      `─────'             
                                              ▔                     ▔                 │         
                                       │Project B                                               
                                        ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
```

## Status

This project is under active development, some apis are experimental and may change between releases.

Draft documentation can be found here: https://docs.clusterless.io/

- [CLI usage](https://docs.clusterless.io/reference/1.0-wip/index.html#commands)
- [Project components](https://docs.clusterless.io/reference/1.0-wip/index.html#components)
- [Project file format](https://docs.clusterless.io/reference/1.0-wip/index.html#models)

See the [ROADMAP](ROADMAP.md) for planned capabilities.

For example scenarios, see

- [clusterless-aws-example](https://github.com/ClusterlessHQ/clusterless-aws-examples) - simple examples to start with
- [aws-s3-log-pipeline](https://github.com/ClusterlessHQ/aws-s3-log-pipeline) - end-to-end sample pipeline for
  processing AWS S3 access logs

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

## Prerequisites

_If you already have Node and npm, skip to the AWS CDK install._

Install [nvm](https://github.com/nvm-sh/nvm):

> curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash

Install node/npm:

> nvm install --lts

Install the AWS CDK:

> npm install -g aws-cdk@2.89.0

after install, make sure to bootstrap your account and region:

https://docs.aws.amazon.com/cdk/v2/guide/bootstrapping.html#bootstrapping-howto

> cdk bootstrap aws://ACCOUNT-NUMBER-1/REGION-1 aws://ACCOUNT-NUMBER-2/REGION-2 ...

Install Java 17 via [sdkman](https://sdkman.io):

> sdk install java 17.0.8-graalce

## Installing Clusterless

The Clusterless CLI app `cls` is downloadable from GitHub releases:

- https://github.com/ClusterlessHQ/clusterless/releases

The `bin` folder will have:

- `cls` - the root CLI interface
- `cls-aws` - the AWS interface only used as an escape hatch and testing

## Running

> cls --help

```text
Usage: cls [-hVv] [-D=<String=String>]... [-P=<providerNames>]... [COMMAND]
  -D, --property=<String=String>
                  key=value properties, will be passed down
  -h, --help      Show this help message and exit.
  -P, --providers=<providerNames>
                  provider substrates to target
  -v, --verbose   Specify multiple -v options to increase verbosity.
                  For example, `-v -v -v` or `-vvv`
  -V, --version   Print version information and exit.
Commands:
  help       Display help information about the specified command.
  config     manage local and global configuration settings
  show       display details about providers, components, and project models
  bootstrap  initialize a cloud provider placement
  deploy     deploy a project into a declared placement
  destroy    destroy a project deployed a declared placement
  diff       compare local project changes with a deployed a declared placement
  local      support for executing workloads locally
  verify     verify project changes with a provider
```

Every region must be bootstrapped by the `cls` app before it can be used:

> cls bootstrap help

To bootstrap a region:

> cls bootstrap --region us-east-1 --account 123456789012 --profile my-profile --stage DEV

If any arguments are missing, `cls` will prompt for them.

Note the `--stage` option creates a namespace for all resources, so you can have multiple environments in the same
account.

## Building (optional)

To build the CLI app locally from source, clone the repo and run:

> ./gradlew installDist

This will create the main CLI interface and any substrate interfaces, [see below](#optional) for the paths.

Currently available are:

- `cls` - the root cli interface
- `cls-aws` - the AWS interface only used as an escape hatch and testing

## Optional

Use [direnv](https://direnv.net) to simplify managing paths:

Add to `.envrc`:

```shell
use sdk java 17.0.8-graalce
PATH_add ~/some_dir/clusterless/clusterless-main/build/install/cls/bin
```
