# Recommendation [NUMBER]: [TITLE]

> Revise during planning; lock at implementation. If wrong, abandon code and iterate RDR.

## Metadata
- **Date**: YYYY-MM-DD
- **Status**: Draft | Final | Implemented | Abandoned | Superseded
  - Draft: During planning/research phase
  - Final: Locked, ready for or during implementation
  - Implemented: Implementation complete
  - Abandoned: RDR not implemented
  - Superseded: Replaced by another RDR
- **Type**: Feature | Bug Fix | Technical Debt | Framework Workaround | Architecture
- **Priority**: High | Medium | Low
- **Related Issues**: [Links to related issues/tickets]
- **Related Tests**: [Test classes or test scenarios]

## Problem Statement

> **Fill this out first when creating the RDR with your initial understanding**
> **Refine this as research deepens your understanding of the problem**

[Clear, concise description of the problem or need this RDR addresses. What is the specific challenge or requirement?]

## Context

> **Fill this out when creating the RDR to establish initial understanding**

### Background
[Provide relevant background information, including:
- How the issue was discovered
- Current system behavior
- Impact on users or system
- Any constraints or requirements]

### Technical Environment
[Describe relevant technical context:
- Framework versions
- Dependencies
- System architecture
- Related components]

## Research Findings

> **Update iteratively during the PLANNING phase as you conduct research**
> **This section will be locked once implementation begins**
>
> **Research Materials to Consult:**
> - Requirements documents and standards documentation
> - Dependency source code (not just public docs)
> - Dependency official documentation
> - Existing codebase patterns and constraints
>
> **Include Evidence:**
> - Citations to specific documents, standards sections, or code locations
> - Code snippets (pseudocode or real code) demonstrating capabilities, limitations, or usage patterns
> - Goal: Capture hard-to-earn data points to avoid future rediscovery

### Investigation Process
[Document the research approach as you go:
- Code analysis performed (existing codebase and dependencies)
- Documentation reviewed (official docs, standards, requirements)
- Dependency source code reviewed
- Experiments conducted
- External resources consulted
- Standards sections referenced]

### Key Discoveries
[Add findings as you discover them with citations and code snippets where applicable:
- Root causes identified
- Framework/dependency capabilities or limitations discovered (include code snippets)
- Hidden or undocumented features found in dependency source
- Performance implications
- Security considerations
- Standards requirements (cite specific sections)
- Requirement constraints (cite specific documents)]

## Proposed Solution

> **Fill this out after exploring alternatives - document your final decision**

### Approach
[Detailed description of the recommended solution]

### Technical Design
[Include relevant technical details:
- Architecture diagrams (if applicable)
- Code structure
- API changes
- Data model changes]

### Decision Rationale
[Explain why this approach was chosen over alternatives:
- Key factors that drove the decision
- How this addresses the problem statement
- Why alternatives were ruled out]

### Implementation Example
```java
// Example code demonstrating key aspects of the solution
```

## Alternatives Considered

> **Document options as you explore them during the PLANNING phase**
> **These alternatives inform the final decision before implementation begins**

### Alternative 1: [Name]
**Description**: [Brief description]
**Pros**:
- [Advantage 1]
- [Advantage 2]
**Cons**:
- [Disadvantage 1]
- [Disadvantage 2]
**Reason for rejection**: [Why this wasn't chosen]

### Alternative 2: [Name]
**Description**: [Brief description]
**Pros**:
- [Advantage 1]
- [Advantage 2]
**Cons**:
- [Disadvantage 1]
- [Disadvantage 2]
**Reason for rejection**: [Why this wasn't chosen]

## Trade-offs and Consequences

### Positive Consequences
- [Benefit 1]
- [Benefit 2]
- [Benefit 3]

### Negative Consequences
- [Drawback 1]
- [Drawback 2]

### Risks and Mitigations
- **Risk**: [Description]
  **Mitigation**: [How to address]
- **Risk**: [Description]
  **Mitigation**: [How to address]

## Implementation Plan

### Prerequisites
- [ ] [Prerequisite 1]
- [ ] [Prerequisite 2]

### Step-by-Step Implementation

#### Step 1: [Title]
[Detailed instructions for this step]

#### Step 2: [Title]
[Detailed instructions for this step]

#### Step 3: [Title]
[Detailed instructions for this step]

### Files to Modify
- `path/to/file1.java` - [Description of changes]
- `path/to/file2.yaml` - [Description of changes]
- `path/to/file3.conf` - [Description of changes]

### Dependencies
- [Any new dependencies to add]
- [Any dependencies to update]

## Validation

### Testing Approach
[Describe how to verify the solution works correctly]

### Test Scenarios
1. **Scenario**: [Description]
   **Expected Result**: [What should happen]

2. **Scenario**: [Description]
   **Expected Result**: [What should happen]

### Performance Validation
[How to verify performance requirements are met]

### Security Validation
[How to verify security requirements are met]

## References

[Cite all sources consulted during research. Include specific sections/pages where applicable.]

### Requirements & Standards
- [Requirement document, section X.Y]
- [Standard name, section Z.A]

### Dependencies
- [Framework/library documentation URL]
- [Source code file paths reviewed: `path/to/file.java:LineNumber`]
- [API documentation URL]

### Research Resources
- [Related issues/tickets]
- [Blog posts, articles, or papers]
- [Stack Overflow discussions or community resources]

## Notes

[Any additional notes, considerations, or future improvements]