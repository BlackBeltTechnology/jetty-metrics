# Jetty Metrics - Project Documentation

## Project Overview


**Repository:** BlackBeltTechnology/jetty-metrics
**License:** Apache License 2.0
**Java Version:** 21
**Build System:** Maven with Maven Wrapper (`./mvnw`), maven-bundle-plugin for OSGi, karaf-maven-plugin for features

1. Provides an OSGi Declarative Services component (`JettyConfigurator`) that instruments Apache Karaf's embedded Jetty server with Dropwizard Metrics
2. Wraps Jetty's HTTP handler with `InstrumentedHandler` from `metrics-jetty9`, capturing request rates, response times, and status code distributions
3. Registers metrics into the Sling-named `MetricRegistry` so they integrate with existing Karaf metrics infrastructure
4. Packages everything as a Karaf feature for one-step deployment via `feature:install jetty-metrics`

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
jetty-metrics/
├── jetty-metrics-impl/          # OSGi bundle — core implementation
│   ├── pom.xml                  # bundle packaging, maven-bundle-plugin
│   └── src/main/java/           # JettyConfigurator component
├── jetty-metrics-karaf-features/ # Karaf feature descriptor
│   ├── pom.xml                  # feature packaging, karaf-maven-plugin
│   └── src/main/feature/        # feature.xml + required-capabilities
├── .github/workflows/           # CI/CD GitHub Actions (build, release, merge)
├── pom.xml                      # Parent POM (dependency management, profiles)
├── logback-test.xml             # Logging config for tests
└── .mvn/                        # Maven wrapper configuration
```

## Core Modules

| Module | Type | Purpose |
|--------|------|---------|
| `jetty-metrics-impl/` | OSGi `bundle` | Contains `JettyConfigurator` — an `@Component(immediate=true)` that creates an `InstrumentedHandler` wired to the Sling MetricRegistry and registers it as an OSGi `Handler` service |
| `jetty-metrics-karaf-features/` | Karaf `feature` | Defines the `jetty-metrics` feature descriptor bundling metrics-core, metrics-jetty9, sling-commons-metrics, and the impl bundle |

## Technology Stack

### Core Technologies
- **OSGi Declarative Services** (SCR) — `@Component`, `@Reference`, `@Activate`, `@Deactivate` annotations
- **Dropwizard Metrics 3.2.6** — `metrics-core` and `metrics-jetty9` for HTTP handler instrumentation
- **Apache Sling Commons Metrics 1.2.6** — provides the named `MetricRegistry` (target filter: `name=sling`)
- **Jetty 9.4.28** — the embedded HTTP server being instrumented (provided scope)
- **Apache Karaf 4.4.7** — OSGi runtime and feature deployment target
- **Lombok 1.18.34** — compile-time code generation (provided scope)
- **Google Guava 30.0-jre** — utility library

### Build & Quality
- **Maven** with `flatten-maven-plugin` for CI-friendly `${revision}` versioning
- **maven-bundle-plugin 5.1.8** for OSGi bundle generation
- **karaf-maven-plugin** for feature descriptor generation and verification
- **JUnit 5.9.1** + **Mockito 4.8.0** + **Hamcrest 2.2** for testing
- **JaCoCo 0.8.12** for code coverage
- **SonarQube** (sonar-maven-plugin 3.9.1) for static analysis
- **maven-surefire-plugin 3.5.1** with `--add-opens` for Java 21 module access

## Build Commands

```bash
# Full build (uses Maven wrapper)
./mvnw clean install

# Build skipping tests
./mvnw clean install -DskipTests

# Build a single module
./mvnw clean install -pl jetty-metrics-impl

# Run tests only
./mvnw test

# Run a single test class
./mvnw test -pl jetty-metrics-impl -Dtest=TestClassName

# Skip all non-impl modules
./mvnw clean install -DskipModules
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Active by default — includes `jetty-metrics-impl` and `jetty-metrics-karaf-features` modules. Deactivate with `-DskipModules=true` |
| `sign-artifacts` | Signs artifacts with `sign-maven-plugin` for release to Maven Central |
| `release-dummy` | Deploys to a local `/tmp/` directory for testing the release process |
| `release-judong` | Deploys to the internal JuDong Nexus snapshot repository |
| `release-central` | Deploys to Maven Central via Sonatype OSSRH with auto-release |
| `generate-github-asciidoc-diagrams` | Generates PNG diagrams from AsciiDoc sources in `.github/` |
| `update-source-code-license` | Updates Apache 2.0 license headers in source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM — dependency management, plugin configuration, all profiles |
| `jetty-metrics-impl/pom.xml` | Impl module — OSGi bundle packaging with maven-bundle-plugin |
| `jetty-metrics-karaf-features/pom.xml` | Feature module — Karaf feature packaging with feature verification |
| `jetty-metrics-karaf-features/src/main/feature/feature.xml` | Karaf feature descriptor listing all bundles to install |
| `jetty-metrics-karaf-features/src/main/feature/feature-required-capabilities.xml` | OSGi capability requirements for feature verification |
| `logback-test.xml` | Logback configuration used during test execution |
| `.mvn/wrapper/maven-wrapper.properties` | Maven wrapper version pinning |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.8+ (or use the included `./mvnw` wrapper)

**Surefire JVM args** (configured automatically):
- `--add-opens java.base/java.lang=ALL-UNNAMED`
- `--add-opens java.base/java.util=ALL-UNNAMED`
- `--add-opens java.base/java.time=ALL-UNNAMED`

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** CI-friendly `${revision}` property, currently `1.0.0-SNAPSHOT`
- **Branch naming:** `feature/JNG-xxx_description`, `bugfix/JNG-xxx_description`, `release/x.y.z`
- **Commit rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)
- **CI:** GitHub Actions workflows handle build, release, PR merging, and changelog generation

## Important Notes

1. The project has only one Java source file: `JettyConfigurator.java` in `jetty-metrics-impl`. All logic is in this single OSGi component.
2. The `@Reference(target = "(name=sling)")` filter means the component requires a MetricRegistry service with the `name=sling` property — this is provided by Apache Sling Commons Metrics at runtime.
3. The Karaf feature descriptor (`feature.xml`) uses Maven property filtering (`${project.version}`, `${dropwizard-metrics-version}`, etc.) — the `maven-resources-plugin` resolves these during build.
4. The `flatten-maven-plugin` rewrites POMs to replace `${revision}` with the actual version, enabling CI-friendly versioning where the version is set via `-Drevision=x.y.z`.
5. Integration tests (`jetty-metrics-itest`) are currently disabled in the parent POM (commented out).

## Related Documentation

- [README.md](README.md) — Project overview with architecture diagrams
- [CONTRIBUTING.md](CONTRIBUTING.md) — Development setup and submission guidelines
- [.github/CIFLOW.md](.github/CIFLOW.md) — CI/CD pipeline and GitFlow branching documentation
