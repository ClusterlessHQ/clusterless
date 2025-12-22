# Recommendation Decisioning Records (RDRs)

RDRs are specification prompts built through iterative research and refinement. Unlike ADRs which document decisions
already made, RDRs evolve during planning as alternatives compete based on evidence until one wins.

**Core Purpose**: Capture both the final solution and supporting evidence to prevent purpose drift during implementation.

## Index

| ID  | Title | Status | Priority |
|-----|-------|--------|----------|

## Workflow

1. **Create** - Document problem at identification with initial constraints
2. **Research** - Investigate, add findings, refine problem statement, explore alternatives
3. **Finalize** - Lock RDR when implementation begins
4. **Implement** - Use locked RDR as specification prompt; do not edit during implementation
5. **Post-Mortem** - Update status; create addendum document for learnings

**If implementation reveals RDR is wrong**: Abandon code, iterate RDR with lessons learned, start fresh.

## When to Create an RDR

- Complex multi-step feature implementation
- Significant technical debt or architectural decisions with trade-offs
- Framework/library workarounds requiring substantial research
- Any work benefiting from documented alternatives before committing to code

## Research Guidance

Consult during planning:
- Requirements and standards documentation (cite specific sections)
- Dependency source code (not just public docs)
- Existing codebase patterns

Include in RDR: Citations, code snippets demonstrating capabilities/limitations, hard-to-earn discoveries.
