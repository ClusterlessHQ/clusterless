# URI Formats

There are a number of standardized URIs formats.

Overall the URI has a protocol, bucket/storage, and a path.

In the case of AWS S3, we could have a URI of the format:

```
s3://state_bucket/some_path/file.txt
```

A primary concern is cost-to-serve, considering object storage, is direct function of how often an API is called, and
how much data is stored.

The following formats are intended to be a mix of human friendly and API frugal.

## Project Metadata

Note, partitions can be named (`state=running`), or value (`running`).

Named partitions have the nice side effect of declaring a schema that external tools can parse.

## Arc State

Arc state, when maintained in an object store, will have the form:

```
{provider-service}://{state-store}/arcs/{project-name}/{project-version}/{arc-name}/{lot}/{state}.arc
```

`s3` is the AWS S3 provider service name.

See [Arc States](adr/0003-arc-state-and-data-metadata.md#arc-states).

Note that an `ls` on `{state-store}/arcs/{project-name}/{project-version}/{arc-name}/{lot}/` will result in a list of
all states associated with the arc.

## Manifest

Manifests may be flat, or hierarchical.

A flat manifest has a single manifest file with all the members of the declared lot.

```
{provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/manifest.{ext}
```

A flat hierarchical has multiple manifests, each within a unique set of partitions.

```
{provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/manifest-root.{ext}
{provider-service}://{manifest-store}/datasets/{dataset-name}/{dataset-version}/{lot}/{state}[/{attempt}]/{part-a}[/{part-b}/...]/manifest.{ext}
```

Note that the `partial` state may have multiple attempts. Consider a workload that fails on retries, and is eventually
fixed or recovers on a later attempt. All the data generated in those attempts should be accounted for and removed.

See [Manifest States](adr/0003-arc-state-and-data-metadata.md#manifest-states).
