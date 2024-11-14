package dev.redstones.moonshine.protocol.slp

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ServerListPing(
    val version: Version,
    val players: Players? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @Serializable(with = JsonAsStringSerializer::class)
    val description: String = "{\"text\":\"\"}",
    val favicon: String? = null,
    val enforcesSecureChat: Boolean = false,
    val previewsChat: Boolean = false
) {

    val legacyDescription: String
        get() {
            val component = JSONComponentSerializer.json().deserialize(description)
            return LegacyComponentSerializer.legacySection().serialize(component)
        }

    @Serializable
    data class Version(
        val name: String = "Old",
        val protocol: Int
    )

    @Serializable
    data class Players(
        val max: Int,
        val online: Int,
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val sample: List<Sample> = emptyList()
    ) {
        @Serializable
        data class Sample(
            val name: String,
            val id: String
        )
    }
}

object JsonAsStringSerializer: JsonTransformingSerializer<String>(String.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        return JsonPrimitive(element.toString())
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        return Json.parseToJsonElement(element.jsonPrimitive.content)
    }
}
