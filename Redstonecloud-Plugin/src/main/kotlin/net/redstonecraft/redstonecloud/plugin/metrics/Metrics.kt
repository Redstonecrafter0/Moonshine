package net.redstonecraft.redstonecloud.plugin.metrics

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonecloud.plugin.RedstonecloudPlugin
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.text.SimpleDateFormat
import java.util.*

@RestController
class MetricsController {

    val custom = mutableMapOf<String, () -> Int>()

    @GetMapping("/apis/custom.metrics.k8s.io/v1beta1/")
    internal fun healthCheck() = "{\"status\": \"healthy\"}"

    @GetMapping("/apis/custom.metrics.k8s.io/v1beta1/namespaces/{namespace}/services/{service}/{metric}")
    internal fun metric(@PathVariable namespace: String, @PathVariable service: String,
                        @PathVariable metric: String): ResponseEntity<String> {
        val value = when (metric) {
            "connections" -> RedstonecloudPlugin.connectionCount
            else -> {
                custom[metric]?.invoke() ?: return ResponseEntity(HttpStatus.NOT_FOUND)
            }
        }
        return ResponseEntity.ok(Json.encodeToString(Metric(namespace, service, metric, value)))
    }

}

@Serializable
data class Metric(val kind: String = "MetricValueList", val apiVersion: String = "custom.metrics.k8s.io/v1beta1",
                  val metadata: Metadata = Metadata(), val items: List<Item>) {

    companion object {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    }

    constructor(namespace: String, name: String, metric: String, value: Int) : this(
        items = listOf(
            Item(
                describedObject = DescribedObject(namespace = namespace, name = name), metricName = metric,
                value = value
            )
        )
    )

    @Serializable
    data class Metadata(val selfLink: String = "/apis/custom.metrics.k8s.io/v1beta1/")

    @Serializable
    data class Item(val describedObject: DescribedObject, val metricName: String,
                    val timestamp: String = df.format(Date()), val value: Int)

    @Serializable
    data class DescribedObject(val kind: String = "Service", val namespace: String, val name: String,
                               val apiVersion: String = "/v1beta1")
}

@Component
class ServerPortCustomizer : WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    override fun customize(factory: ConfigurableWebServerFactory) {
        factory.setPort(80)
    }
}

@SpringBootApplication
open class MetricsApplication

fun startMetricsServer() = runApplication<MetricsApplication>()

fun main() {
    startMetricsServer()
}
