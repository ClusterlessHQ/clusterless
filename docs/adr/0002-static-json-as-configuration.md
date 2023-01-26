# 2. Static JSON as configuration

Date: 2023-01-26

## Status

Accepted

## Context

Declarative configuration can be complex and non-intuitive. There are a large number of configuration languages like
yaml, json, hocon, xml, properties, etc. At a certain point, the flexibility of the syntax tends to towards being an
actual language, possibly even being turing complete.

## Decision

It is out of the scope of this application to present and implement a complex language for maintaining configurations.

This application main code will only load a static JSON schema, that is, it will not support text substitution,
references to other block, or includes.

For example, the `project.json` could be implemented to support a target of multiple regions across multiple accounts.

But many resources that will be deployed will need their physical/global names updated to include a value (like the
region name or account id) that prevents collisions on deployment. If the project declares an S3 bucket, but there
are multiple target regions declared, the provisioning of the S3 bucket will fail on the second region because of
a conflict against the first.

Tools and languages like [Dhall](https://dhall-lang.org) or [Jsonnet](https://jsonnet.org) provide the user a means
to build modular configuration stacks that can be rendered into a final set of JSON files suitable to be read by
this application.

## Consequences

Improved testing and stability will be the positive result of this decision. Time won't be spending testing capabilities
readily available in other tools.

But in order to improve the user experience, plugin support for an independently maintained configuration language
may be required. This can happen at the root application entrypoint, wrapper script.
