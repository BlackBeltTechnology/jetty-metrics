# jetty-metrics-impl Specification

## Purpose

Provides an OSGi Declarative Services component (`JettyConfigurator`) that instruments Karaf's embedded Jetty server with Dropwizard Metrics, enabling HTTP request/response metric collection through the Sling MetricRegistry.

## Architecture

The module contains a single immediate OSGi component:

- **`JettyConfigurator`** (`@Component(immediate = true)`) — the entry point that wires Dropwizard's `InstrumentedHandler` to the Sling `MetricRegistry` and registers it as an OSGi `Handler` service.

Key classes involved (from dependencies):
- `com.codahale.metrics.MetricRegistry` — the Dropwizard metrics registry (injected via `@Reference(target = "(name=sling)")`)
- `com.codahale.metrics.jetty9.InstrumentedHandler` — Jetty handler wrapper that records HTTP metrics
- `org.eclipse.jetty.server.Handler` — Jetty's handler interface, registered as an OSGi service

## Requirements

### Requirement: Component SHALL activate and register an instrumented handler

The `JettyConfigurator` component SHALL create an `InstrumentedHandler` wrapping the Sling MetricRegistry and register it as an OSGi `Handler` service upon activation.

#### Scenario: Successful activation with available MetricRegistry

- **GIVEN** an OSGi runtime with a `MetricRegistry` service having the property `name=sling`
- **WHEN** the `JettyConfigurator` component is activated
- **THEN** an `InstrumentedHandler` is created with the prefix `"JettyMetrics"`
- **AND** the handler is registered as an OSGi `Handler` service via `BundleContext.registerService()`

### Requirement: Component SHALL require a Sling-named MetricRegistry

The `JettyConfigurator` SHALL depend on a `MetricRegistry` service filtered by `(name=sling)` using OSGi `@Reference`.

#### Scenario: MetricRegistry not available

- **GIVEN** no `MetricRegistry` service with property `name=sling` is registered
- **WHEN** the OSGi framework attempts to satisfy the `JettyConfigurator` component
- **THEN** the component SHALL NOT activate (OSGi DS will hold it in unsatisfied state)

### Requirement: Component SHALL cleanly deactivate

The `JettyConfigurator` SHALL unregister the `Handler` service upon deactivation.

#### Scenario: Normal deactivation

- **GIVEN** the `JettyConfigurator` component is active and has registered a `Handler` service
- **WHEN** the component is deactivated
- **THEN** the `ServiceRegistration.unregister()` method is called
- **AND** the instrumented handler is removed from the OSGi service registry

#### Scenario: Deactivation when registration is null

- **GIVEN** the `JettyConfigurator` component is being deactivated
- **WHEN** the `registerService` field is null (activation failed or was never called)
- **THEN** no `NullPointerException` is thrown — the null check prevents it

### Requirement: Instrumented handler SHALL use correct metrics prefix

The `InstrumentedHandler` SHALL be created with the metrics prefix `"JettyMetrics"` to namespace all collected metrics.

#### Scenario: Metrics naming

- **GIVEN** the `JettyConfigurator` has activated successfully
- **WHEN** HTTP requests are processed through the `InstrumentedHandler`
- **THEN** metrics are recorded under the `"JettyMetrics"` prefix in the Sling MetricRegistry
