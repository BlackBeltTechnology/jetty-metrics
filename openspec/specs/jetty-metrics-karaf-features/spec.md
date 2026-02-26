# jetty-metrics-karaf-features Specification

## Purpose

Defines the Karaf feature descriptor for the jetty-metrics project, enabling one-step deployment of the metrics instrumentation bundle and all its transitive dependencies into an Apache Karaf container.

## Architecture

This module produces a Karaf feature XML descriptor (no Java code). It contains:

- **`feature.xml`** — defines the `jetty-metrics` feature with its bundle dependencies and prerequisite features
- **`feature-required-capabilities.xml`** — declares OSGi capabilities required for feature verification (SCR extender 1.3.0)

The feature depends on standard Karaf features (`pax-jetty`, `scr`, `http`) and bundles three external JARs plus the impl bundle.

## Requirements

### Requirement: Feature SHALL declare all required bundles

The `jetty-metrics` feature descriptor SHALL list all bundles needed to run the metrics collector.

#### Scenario: Feature bundles are complete

- **GIVEN** the `jetty-metrics` Karaf feature is defined in `feature.xml`
- **WHEN** the feature is inspected
- **THEN** it includes the following bundles:
  - `mvn:io.dropwizard.metrics/metrics-core/3.2.6`
  - `mvn:org.apache.sling/org.apache.sling.commons.metrics/${sling-metrics-version}`
  - `mvn:io.dropwizard.metrics/metrics-jetty9/${dropwizard-metrics-version}`
  - `mvn:hu.blackbelt/jetty-metrics-impl/${project.version}`

### Requirement: Feature SHALL declare prerequisite Karaf features

The feature SHALL depend on `pax-jetty`, `scr`, and `http` features to ensure Jetty, OSGi Declarative Services, and the HTTP service are available.

#### Scenario: Feature dependencies

- **GIVEN** the `jetty-metrics` feature descriptor
- **WHEN** the feature prerequisites are checked
- **THEN** `pax-jetty` is declared as a dependency feature
- **AND** `scr` is declared as a required feature
- **AND** `http` is declared as a required feature

### Requirement: Feature SHALL pass Karaf verification

The feature descriptor SHALL pass `karaf-maven-plugin:verify` against the Karaf 4.4.7 framework, standard, and enterprise feature repositories.

#### Scenario: Feature verification during build

- **GIVEN** the `verify-feature` profile is active (default)
- **WHEN** `./mvnw clean install` is run
- **THEN** the `karaf-maven-plugin` verify goal succeeds with Java SE 21
- **AND** the feature resolves against `framework`, `standard`, and `enterprise` Karaf feature repositories

### Requirement: Required capabilities SHALL declare SCR extender

The `jetty-metrics-required-capabilities` feature SHALL declare the `osgi.component` extender capability (version 1.3.0) to satisfy OSGi resolver requirements during verification.

#### Scenario: SCR capability declaration

- **GIVEN** `feature-required-capabilities.xml` is included in the verification descriptors
- **WHEN** feature verification runs
- **THEN** the `osgi.extender;osgi.extender=osgi.component;version=1.3.0` capability is available
- **AND** the impl bundle's SCR requirement is satisfied
