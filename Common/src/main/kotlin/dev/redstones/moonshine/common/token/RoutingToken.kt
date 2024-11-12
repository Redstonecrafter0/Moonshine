package dev.redstones.moonshine.common.token

import dev.redstones.moonshine.common.util.fromBase64ToByteArray
import dev.redstones.moonshine.common.util.toBase64
import org.bouncycastle.crypto.generators.Ed448KeyPairGenerator
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed448Signer
import java.security.SecureRandom
import java.time.Instant

/**
 * example: eu-central-1-lobby-abc123
 *
 * [region], [subregion] and [availabilityZone] identify the cluster. example: eu-central-1
 * [gamemode] is the name of the headless service and pods subdomain.
 * [serverId] is the name of the pod. if omitted the headless service is queried
 * */
class RoutingToken(
    val region: String,
    val subregion: String,
    val availabilityZone: String,
    val gamemode: String,
    val serverId: String? = null
) {

    private constructor(payload: List<String>): this(
        payload[0],
        payload[1],
        payload[2],
        payload[3],
        if (payload.size == 5) payload[4] else null
    )

    constructor(payload: String): this(
        payload.split("-")
            .also { if (it.size != 4 && it.size != 5) throw IndexOutOfBoundsException() }
    )

    class Verifier(private val publicKey: Ed448PublicKeyParameters) {

        constructor(publicKey: ByteArray): this(Ed448PublicKeyParameters(publicKey))

        companion object {
            fun randomVerifier(): Verifier {
                val privateKey = ByteArray(Ed448PrivateKeyParameters.KEY_SIZE)
                SecureRandom.getInstanceStrong().nextBytes(privateKey)
                return Verifier(Ed448PrivateKeyParameters(privateKey).generatePublicKey())
            }
        }

        private val signer = Ed448Signer(ByteArray(0))

        fun verify(token: String): RoutingToken? {
            val parts = token.split(".")
            if (parts.size != 3) {
                return null
            }
            val (payload, timestampRaw, signature) = parts
            val timestamp = Instant.ofEpochSecond(timestampRaw.toLong())
            if (timestamp.plusSeconds(10).isAfter(Instant.now())) {
                return null
            }
            val token = try {
                RoutingToken(payload)
            } catch (_: IndexOutOfBoundsException) {
                return null
            }
            signer.init(false, publicKey)
            val message = "$payload.$timestampRaw".encodeToByteArray()
            signer.update(message, 0, message.size)
            if (signer.verifySignature(signature.fromBase64ToByteArray())) {
                return token
            }
            return null
        }

    }

    /**
     * @param privateKey must be 57 random bytes
     * */
    fun sign(privateKey: ByteArray): String {
        val key = Ed448PrivateKeyParameters(privateKey)
        val signer = Ed448Signer(ByteArray(0))
        signer.init(true, key)
        val messageText = "${toString()}.${Instant.now().epochSecond}"
        val message = messageText.encodeToByteArray()
        signer.update(message, 0, message.size)
        return "$messageText.${signer.generateSignature().toBase64()}"
    }

    override fun toString(): String {
        return "$region-$subregion-$availabilityZone-$gamemode" + if (serverId != null) "-$serverId" else ""
    }

}
