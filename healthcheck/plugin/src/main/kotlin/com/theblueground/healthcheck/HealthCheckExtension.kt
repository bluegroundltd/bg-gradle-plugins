package com.theblueground.healthcheck

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * [HealthCheckExtension] holds configuration values for [HealthCheckTask].
 *
 * Inputs
 * ======
 *
 * @property healthEndpoint the endpoint to check for Docker app started status.
 * @property retries the number of retries when checking Docker app startup. - default: 20
 * @property intervalSecs the interval in seconds, between retries of Docker app startup check. - default: 1
 *
 * */
@Suppress("UnstableApiUsage")
open class HealthCheckExtension @JvmOverloads constructor(
  // Needed for Gradle
  @get:Internal
  internal val name: String = "default",
  objects: ObjectFactory
) {

  companion object {
    const val NAME = "healthCheckExtension"
    private const val DEFAULT_NUM_OF_RETRIES = 20
    private const val DEFAULT_INTERVAL_SECS = 1
  }

  @get:Input
  val healthEndpoint: Property<String> = objects.property(String::class.java)

  @get:Input
  val retries: Property<Int> = objects.property(Int::class.java)

  @get:Input
  val intervalSecs: Property<Int> = objects.property(Int::class.java)

  /*
  * Specify sensible defaults, for the number of retries and interval seconds.
  * Health endpoint is a hard requirement, so no default value provided for it.
  * Its absence of value is being checked before triggering the task.
  * */
  init {
    retries.convention(DEFAULT_NUM_OF_RETRIES)
    intervalSecs.convention(DEFAULT_INTERVAL_SECS)
  }
}
