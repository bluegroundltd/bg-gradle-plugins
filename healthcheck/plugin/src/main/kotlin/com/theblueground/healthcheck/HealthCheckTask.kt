package com.theblueground.healthcheck

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * [HealthCheckTask] is responsible to trigger a Worker in order to check if the
 * app configured through [HealthCheckExtension] is healthy.
 *
 * @param extension the lazy configured [HealthCheckExtension] from each sub-project.
 * */
@Suppress("UnstableApiUsage")
internal abstract class HealthCheckTask @Inject constructor(
  private val extension: HealthCheckExtension
) : DefaultTask() {

  companion object {
    const val NAME = "healthCheck"
  }

  @TaskAction
  fun checkHealth() {
    require(extension.healthEndpoint.get().isNotBlank()) {
      "A `healthEndpoint` should be specified in order to utilize this task."
    }

    /**
     * Trigger Worker with the values configured in [HealthCheckExtension]
     * */
    project.serviceOf<WorkerExecutor>()
      .noIsolation()
      .submit(HealthCheckWorkAction::class.java) {
        healthEndpoint.set(extension.healthEndpoint.get())
        retries.set(extension.retries.get())
        intervalSecs.set(extension.intervalSecs.get())
      }
  }

  /**
   * Worker parameters that will be injected in [HealthCheckWorkAction]
   *
   * @property healthEndpoint the endpoint to check for Docker app started status.
   * @property retries the number of retries when checking Docker app startup.
   * @property intervalSecs the interval in seconds, between retries of Docker app startup check.
   * */
  @Suppress("UnstableApiUsage")
  interface HealthCheckWorkParameters : WorkParameters {
    val healthEndpoint: Property<String>
    val retries: Property<Int>
    val intervalSecs: Property<Int>
  }

  /**
   * [HealthCheckWorkAction] is responsible to loop for the given number of retries, in order to check
   * health status on the given [HealthCheckWorkParameters.healthEndpoint].
   *
   * In order to do that, it performs a GET request and checks if the response code is a success 200.
   * If the response code is not a success 200, it will sleep for [intervalMillis] and will retry.
   * */
  @Suppress("UnstableApiUsage")
  abstract class HealthCheckWorkAction : WorkAction<HealthCheckWorkParameters> {

    private val logger: Logger = Logging.getLogger(
      "Blueground:HealthCheck:${HealthCheckTask::class.java.simpleName}"
    )

    private val healthEndpoint = parameters.healthEndpoint.get()
    private val retries = parameters.retries.get()
    private val intervalSecs = parameters.intervalSecs.get()
    private val intervalMillis = intervalSecs * 1000L

    override fun execute() {

      sleep() // sleep before first check

      for (n in retries downTo 1) {
        val (_, _, result) = healthEndpoint
          .httpGet()
          .responseString()

        when(result) {
          is Result.Failure -> {
            logger.quiet("${result.error.message}. Retry in $intervalSecs sec. Remaining retries: $n")
          }
          is Result.Success -> {
            logger.quiet("Service started!")
            return
          }
        }

        sleep() // sleep before next retry
      }

      throw GradleException("Service failed to start in ${retries * intervalSecs} seconds")
    }

    private fun sleep() {
      Thread.sleep(intervalMillis)
    }
  }
}
