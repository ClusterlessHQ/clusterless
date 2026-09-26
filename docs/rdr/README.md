<!--
  RDR-HOME index template. /rdr-init (stages/00-bootstrap.md, step 4) copies this
  to $RDR_RECORDS/README.md when a consumer's RDR home has no index yet. It is the
  ONLY engine file vendored into a consumer — TEMPLATE.md and the prompts are
  read in place from $RDR_HOME, never copied (see skills/rdr-common.md
  on TEMPLATE drift). After the copy this is a project-local file: edit it freely;
  it does not track the engine.

  Fill clusterless below. /rdr-seed adds the index row for a new RDR (at Draft);
  /rdr-finalize flips that row's Status to Final at lock; intermediate stages keep
  the Status/Priority columns current (this file is the authoritative status table
  — see stages/07.0-finalize.md). Leave the table header + legend in place; the
  flow reads this file as the index + Status/Priority table.
-->
# clusterless — Recommendation Decisioning Records

Project-scoped RDRs for `clusterless`. Draft new RDRs from the shared
`TEMPLATE.md` in the RDR engine (`$RDR_HOME/TEMPLATE.md`); `/rdr-seed`
materializes a copy automatically. Rationale + the full stage flow live in the
engine README — this file is only the per-project index.

## Index

| ID | Title | Status | Priority |
| --- | --- | --- | --- |
<!-- /rdr-seed adds a row per RDR here (at Draft); first seeded RDR replaces this comment. -->

## Status legend

- **Draft** — during the planning/research phase
- **Final** — locked, ready for or during implementation
- **Implemented** — implementation complete
- **Reverted** — implemented then undone (document why)
- **Abandoned** — RDR not implemented
- **Superseded** — replaced by another RDR
- **Demoted** — judged not RDR-shaped; refiled as a plain issue (carry `Demoted [→ <issue link>]`)
- **Deferred** — parked before Lock; no acceptable mechanism exists yet (carry `Deferred [revisit when <condition>]`).
  A pause, not an exit — no post-mortem is owed and the RDR re-enters when the trigger fires
