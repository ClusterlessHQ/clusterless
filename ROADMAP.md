# Roadmap

Things not yet implemented.

# CLI

Most things in the cli have yet to be implemented.

- https://docs.clusterless.io/reference/1.0-wip/index.html#commands
- [CommandLineUsage](docs/CommandLineUsage.adoc)

# Capabilities

1. Packaging
   1. Downloadable/installable self-contained binary
      1. https://github.com/ClusterlessHQ/clusterless/releases
2. Improved S3CopyArc
   1. Object renaming
   2. Basic static partitioning
   3. Include/exclude predicates
3. Custom Lambda based arc workloads
4. High frequency S3 listener boundary
   1. For aggregating objects that arrive within a lot interval
5. Native resources and workloads
   1. AWS Glue database and catalog updates
   2. AWS Athena CTAS/INSERT INTO queries (for chaining SQL)
   3. AWS Sagemaker training/validation
6. Common data processing workloads
   1. Data reformatting (from text/json to binary/parquet)
      2. https://github.com/ClusterlessHQ/tessellate
   2. Dynamic data repartitioning (partitions based on data like timestamps)
      3. https://github.com/ClusterlessHQ/tessellate
   3. Predicate/duplicate index creation and data filtering
7. Join Barrier implementations
8. Scheduled arc executions
   1. Some arcs may need to run periodically
9. Parallelized workloads
   1. Workloads can be parallelized on source partitions
10. Pluggable modules for providing third-party services
11. Localstack support for faster testing AWS scenarios
12. Alternate substrates/providers
