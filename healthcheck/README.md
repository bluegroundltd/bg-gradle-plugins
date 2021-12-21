# Healthcheck Plugin

This plugin allows you to do health checks on a specified endpoint

https://plugins.gradle.org/plugin/com.theblueground.healthcheck

## Installation

### Kotlin

```kotlin
plugins {
  id("com.theblueground.healthcheck") version "0.0.1"
}
```

### Groovy

```groovy
plugins {
  id "com.theblueground.healthcheck" version "0.0.1"
}
```

## Usage

In order to use this plugin you need to "hook" your task on the `healthCheck` task.

Consider the following example:

```kotlin
// Access [HealthCheckTask]
tasks.named("healthCheck") {
  description = "Checks health status of the service."
  group = LifecycleBasePlugin.VERIFICATION_GROUP
}

// Configure [HealthCheckExtension]
healthCheckExtension {
  retries.set(50) // default value: 20
  intervalSecs.set(5) // default value: 1
  healthEndpoint.set("http://localhost:8070/actuator/health")
}
```

Now if we have a task responsible to execute our functional tests, we can hook it on the 
`healthCheck` task, like so:

```kotlin
tasks.register<Test>("functionalTests") {
  dependsOn(tasks.named("healthCheck"))
}
```
