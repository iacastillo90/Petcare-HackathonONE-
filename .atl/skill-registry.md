# Skill Registry - Petcare Hackathon Project

**Generated**: 2026-03-20
**Project**: Petcare Hackathon (Spring Boot 3.5.4 + Java 21)

---

## Project Skills (auto-detected from AGENTS.md)

### Framework/Library Detection

| Context | Skill to Load |
|---------|---------------|
| Go tests, Bubbletea TUI testing | `go-testing` |
| Creating new AI skills | `skill-creator` |
| Java Spring Boot 3 development | `spring-boot-3` |
| Java 21 patterns | `java-21` |

---

## Available Skills (Global)

### SDD Workflow Skills
| Skill | Description | Location |
|-------|-------------|----------|
| `sdd-init` | Initialize SDD in project | `~/.config/opencode/skills/sdd-init/` |
| `sdd-explore` | Explore and investigate ideas | `~/.config/opencode/skills/sdd-explore/` |
| `sdd-propose` | Create change proposals | `~/.config/opencode/skills/sdd-propose/` |
| `sdd-spec` | Write specifications | `~/.config/opencode/skills/sdd-spec/` |
| `sdd-design` | Technical design documents | `~/.config/opencode/skills/sdd-design/` |
| `sdd-tasks` | Break down into tasks | `~/.config/opencode/skills/sdd-tasks/` |
| `sdd-apply` | Implement tasks | `~/.config/opencode/skills/sdd-apply/` |
| `sdd-verify` | Validate implementation | `~/.config/opencode/skills/sdd-verify/` |
| `sdd-archive` | Archive completed changes | `~/.config/opencode/skills/sdd-archive/` |

### Java/Spring Boot Skills
| Skill | Description | Location |
|-------|-------------|----------|
| `spring-boot-3` | Spring Boot 3 patterns | `~/.config/opencode/skills/spring-boot-3/` |
| `java-21` | Java 21 patterns (records, sealed types) | `~/.config/opencode/skills/java-21/` |
| `golang-gin-api` | Go Gin REST APIs | `~/.config/opencode/skills/golang-gin-api/` |

### Other Skills
| Skill | Description | Location |
|-------|-------------|----------|
| `skill-registry` | Create/update skill registry | `~/.config/opencode/skills/skill-registry/` |
| `github-pr` | Create high-quality PRs | `~/.config/opencode/skills/github-pr/` |
| `jira-epic` | Create Jira epics | `~/.config/opencode/skills/jira-epic/` |
| `jira-task` | Create Jira tasks | `~/.config/opencode/skills/jira-task/` |

---

## How to Use Skills

1. **Detect context** from user request or current file being edited
2. **Load the relevant skill(s)** BEFORE writing code using the `skill` tool
3. **Apply ALL patterns and rules** from the skill
4. Multiple skills can apply when relevant

Example:
```
Task: Writing a new Spring Boot service
→ Load skill: spring-boot-3
→ Follow all patterns from that skill
```

---

## Project Conventions Summary

### Code Style
- Package naming: `com.Petcare.Petcare.*` (lowercase)
- Classes: PascalCase (`UserServiceImplement`)
- Methods: camelCase (`findByEmail`)
- Service implementations: suffix `Implement`
- DTOs: PascalCase with `Request`/`Response` suffix

### Architecture
- **Pattern**: Layered (Controller → Service → Repository → Model)
- **Transaction boundaries**: Always use `@Transactional` on service methods
- **DTO pattern**: Use factory methods (`fromEntity()`, `of()`)

### Testing
- Framework: JUnit 5 + Mockito + AssertJ
- Naming: `{methodName}_{scenario}_{expectedResult}`
- Location: `src/test/java/com/Petcare/Petcare/`

### Build Commands
```bash
./gradlew bootRun     # Start dev server
./gradlew test        # Run all tests
./gradlew build       # Build application
./gradlew spotlessApply  # Auto-fix formatting
```

---

## SDD Integration

This project uses **Spec-Driven Development (SDD)** with **Engram** persistence.

| Artifact | Storage |
|----------|---------|
| SDD artifacts | Engram (topic_key pattern: `sdd/{change}/...`) |
| Project context | Engram (topic_key: `sdd-init/petcare-hackathon`) |
| Skill registry | `.atl/skill-registry.md` (this file) |

**SDD Commands**:
- `/sdd-init` - Initialize SDD context
- `/sdd-explore <topic>` - Investigate feature/requirement
- `/sdd-new <change>` - Start new change (explore + propose)
- `/sdd-ff <change>` - Fast-forward (propose → spec → design → tasks)
- `/sdd-apply <change>` - Implement tasks
- `/sdd-verify <change>` - Validate implementation
- `/sdd-archive <change>` - Archive completed change
