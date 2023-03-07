# 5. URI Types and Naming

Date: 2023-03-03

## Status

Accepted

## Context

The identifiers that reference data handled by the overall system and extensions must be consistently formatted.

But more importantly, the purpose of the identifier must be easily distinguishable.

A common identifier syntax is the [URI](https://www.rfc-editor.org/rfc/rfc3986).

But when an uri is provided, does it reference an object, does it reference a position in a hierarchy, or is it intended
to be used as a predicate when filtering a set of uris for membership?

This is especially important when constructing a declaration or when consuming an API.

For example, manifest files for a given project may all reside in the same AWS S3 bucket, but have unique prefixes that
uniquely identify the location of all manifests for a given project of a specific version.

If an api hands the parameter `manifest` to a developer, how are they to interpret it? As the location of all manifests
or a reference to an individual manifest, without testing the uri by value (parsing it) or by making a remote call?

A naming scheme and convention needs to be created to simplify the role an identifier takes that isn't context
dependent.

## Decision

The URI syntax, as defined in this [RFC](https://www.rfc-editor.org/rfc/rfc3986), will be adopted to format and share
identifiers.

In the case of an AWS bucket, we will use the syntax:

> s3://bucket-name/key

Where `s3` designates the protocol and service type (AWS S3). `bucket-name` is the name of the S3 bucket. And `key`
is a path like identifier that may reference a stored object, or simply be a common prefix to a set of objects. Note
a `key` value will be composed of a hierarchy, like `data/2023/01/01/customers.json`.

The goal here is to disambiguate the purpose of an identifier by adopting a consistent naming scheme and imposing a
consistent format.

There are at least three purposes of an identifier that have been found:

- Identify a single object stored in the service
- Identify a well-formed common root location for one or more objects hierarchically stored in the service
- Provide a predicate prefix that is sufficient to identify a subset of objects in a larger set of objects

For example:

- `s3://manifests/project-a/20230101/` references a location where manifests can be stored, but does not reference an
  individual manifest
- `s3://manifests/project-a/20230101/lot=20230227PT5M287/manifest.json` references a single object that can be retrieved
  from an S3 bucket, or the key in which to store an object as
- `s3://data/project-a/20230101/year=2023/month=02/dept=sales/` would reference a location in a hierarchy for some
  corpus
- `s3://data/project-a/20230101/year=2023/month=02/dept=sales/lot=20230227PT5M287/1672613358000-0-2.parquet` references
  a single data object that can be retrieved
- `s3://data/project-a/20230101/year=2023/month=02/dept=sales/lot=20230227PT5M287/1672613358000-0` would identify the
  prior uri as it shares a common prefix, but itself couldn't be retrieved, nor is it well-formed (a partial filename)

This last example is important. A process writing a file may not know the exact value of the path/location it is writing
too. So if it is creating a manifest file of the data it is accountable for, it may only be able to provide a prefix
that must be resolved into one or more full object identifiers at a later stage.

From the above, we can single out:

- object identifier - well-formed path referencing a single retrievable object
- location path - a well-formed path into a hierarchy
- prefix - a partial path to one or more objects, for use as a predicate to filter larger sets

Where "well-formed" means that path uses hierarchy delimiters (`/`) appropriately.

- object identifier - _does not_ end with a delimiter
- location path - _does_ end with a delimiter
- prefix - _does not_ end with a delimiter

Regardless, both object identifiers and path locations can be used as a prefix. But a prefix cannot be safely used in
either of the other roles.

The naming conventions should subsequently be:

- `*Path` for location paths -- `manifestPath`
- `*Identifier` or `*` for object identifiers --  `manifestIdentifier` or `manifest`
- `*Prefix` for prefixes -- `inputPrefix`

## Consequences

The intent of these roles is to reduce confusion when naming an identifier `manifestPath` vs `manifestIdentifier`.

There will be a cases where a list of URIs will be provided (as the members of a manifest), the list will need to be
stereotyped as a list of:

- `objects`
- `locations`
- `prefixes`

Leading to an Enum of `URIType`:

- identifier
- path
- prefix

In the case of S3, api calls to S3 behave differently if the role of the key being supplied to the api is not properly
disambiguated.

Subsequently, this mechanism will allow the reader to handle the values appropriately. That is, when given a list of
prefix uris, the full identifier may need to be resolved by requesting a listing of keys for object that match the
prefix.