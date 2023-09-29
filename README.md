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

- [How To](https://docs.clusterless.io/guide/1.0-wip/howtos/index.html) - step-by-step guides
- [clusterless-aws-example](https://github.com/ClusterlessHQ/clusterless-aws-examples) - simple examples to start with
- [aws-s3-log-pipeline](https://github.com/ClusterlessHQ/aws-s3-log-pipeline) - end-to-end sample pipeline for
  processing AWS S3 access logs

## About

Clusterless is a tool for deploying decentralized, scalable, and secure data-processing workloads for continuously
arriving data, across clouds.

By leveraging native pay-as-you-go primitives, no runtimes or dedicated services need to be managed.

Zero data arriving means zero costs (other than storage for historical data).

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
- Native services (AWS SageMaker, AWS Athena, AWS Glue, etc.)

Where the intent isn't to have functional parity across substrates, but to ease secure and reliable interoperability
between them.

Currently supported cloud substrates:

- AWS

## Installing

See: https://docs.clusterless.io/guide/1.0-wip/install-quickstart.html

## Running

> cls --help

```text
Usage: cls [-hVv] [--output=<format>] [-D=<String=String>]...
           [-P=<providerNames>]... [COMMAND]
  -D, --property=<String=String>
                          Optional key=value properties, will be passed down.
  -h, --help              Show this help message and exit.
      --output=<format>   Print results using given format.
                          Values: table, json, csv, tsv (default: table)
  -P, --providers=<providerNames>
                          Provider substrates to target.
  -v, --verbose           Specify multiple -v options to increase verbosity.
                          For example, `-v -v -v` or `-vvv`
  -V, --version           Print version information and exit.
Commands:
  help        Display help information about the specified command.
  config      Manage local and global configuration settings.
  show        Display details about providers, components, and project models.
  bootstrap   Initialize a cloud provider placement.
  deploy      Deploy a project into a declared placement.
  destroy     Destroy a project deployed a declared placement.
  diff        Compare local project changes with a deployed a declared
                placement.
  local       Support for executing workloads locally in a terminal.
  verify      Verify project changes with a provider.
  placements  List all visible placements with deployments.
  projects    List all deployed projects.
```

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
