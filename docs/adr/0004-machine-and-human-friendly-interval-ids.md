# 4. machine and human friendly interval ids

Date: 2023-02-11

## Status

Accepted

## Context

Events may arrive for extended periods (years) and continuously (every millisecond, second, minute, etc). To be
manageable, they must be aggregated into discrete and deterministic groups.

Batch ids are common place, but are frequently simple timestamps without meaningful semantics. As an aside, a batch
often designates an process execution, not always collection of things.

The value of common timestamps is that they are machine friendly, but unless formatted, they are hard for humans to
reason about.

A collection of events itself can be anything from a days worth, to a minutes worth, to the events that arrived within a
given second of the minute. That said, daily collections are simple, a resonable text id would be `2023-01-01`
or `20230101`.

Collections or batches of events arriving within a minute or a second interval may questionably be called a batch. Some
have used the term 'micro-batch'.

5, 10, and 15 minute intervals are human scale, and don't affect the overal latency of most systems appreciably. These
could arguably be called 'meso-batch'.

There needs to be a standard way to label a collection of events that arrive within a given interval, an interval id.

## Decision

The simplest interval id would be for daily batches, `20230101`.

Subdividing a day is where it becomes less intuitive.

It should be easy to reason about events that are aggregated in 5 minute intervals. Where to reason about such things
entails identifying completeness or gaps, or even the time of day at a glance.

5 minute intervals happen 12 times an hour, in contrast to the 15 minute interval, a quarter hours, happens 4 times. And
since we typical reason and worry about our data in daily increments, it makes sense to identify a way to easily capture
the day the events arrived and the sub-interval of the day.

Consider the two common formats, `1672546200` and `2023-01-01T04:10:00+00:00`.

The first, duration since the epoch, is not human friendly. The second is helpful when identifying the time of day. But
neither denotes the actual duration captured in the aggregation. Is this a 5 or 10 minute interval?

It is proposed to have a textual id like `20230101PT5M050`.

The first segment of the id is obviously the year, month, and day the events were collected, `20230101`.

The second segment, `PT5M`, is the ISO format of a 5
minute [duration](https://en.wikipedia.org/wiki/ISO_8601#Durations).

The last segment is the ordinal of the interval within the day.

Where the ordinal `50` denotes that `50*5=250` minutes have passed during the day, or that the interval started at 4:10
am. Given the ordinal makes identifying the time of day challening, it has a few virtues.

Knowing the interval duration, we know how many 5 minutes intervals are in a day, 288 (000-287). Thus it is easy not
only to visually identify gaps in a serious of collections, but if the day is complete.

The ordinal is easy to render, just add 1 to the previous value. Consider writing tools in bash that manipulate these
values.

We suggest a format of the form:

> yyyyMMdd'PT'm'M'000

where `000` denotes the ordinal of the interval within the day.

Further, we suggest:

- Fourths - a 15-minute duration, there are 4 Fourths in an hour, and 96 Fourths in a day.
- Sixths - a 10-minute duration, there are 6 Sixths in an hour, and 144 Sixths in a day.
- Twelfths - a 5-minute duration, there are 12 Twelfths in an hour, and 288 Twelfths in a day.

## Consequences

These are non-standard formats, but should be easy to parse using non-specialized tools.

As noted above, the time of day is not obvious, but the interval id may be accompanied by fully human friendly
timestamp. The need for embedding the interval duration in the value takes precedence.
