package com.github.huymaster.textguardian.core.codec

import com.github.huymaster.textguardian.core.lib.LibOpus
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import java.nio.ByteBuffer
import java.nio.ShortBuffer

class OpusCodec(
    private val sampleRate: Int = 48000,
    private val channels: Int = 2,
    private val frameSizeMs: Int = 20
) : AutoCloseable {
    private var encoder: Pointer? = null
    private var decoder: Pointer? = null

    val frameSize = (sampleRate * frameSizeMs / 1000)

    private val maxPayloadSize = 4000

    init {
        val err = IntByReference()

        encoder = LibOpus.INSTANCE.opus_encoder_create(
            sampleRate, channels, LibOpus.OPUS_APPLICATION_VOIP, err
        )
        if (err.value != LibOpus.OPUS_OK || encoder == null) {
            throw kotlin.RuntimeException("Failed to create Opus Encoder: error code ${err.value}")
        }

        decoder = LibOpus.INSTANCE.opus_decoder_create(sampleRate, channels, err)
        if (err.value != LibOpus.OPUS_OK || decoder == null) {
            throw kotlin.RuntimeException("Failed to create Opus Decoder: error code ${err.value}")
        }
    }

    fun encode(pcmData: ShortArray): ByteArray? {
        if (pcmData.size != frameSize) {
            println("Warning: PCM buffer size must be exactly $frameSize samples")
            return null
        }

        val pcmBuffer = ShortBuffer.wrap(pcmData)
        val outputBuffer = ByteBuffer.allocateDirect(maxPayloadSize)

        val encodedBytes = LibOpus.INSTANCE.opus_encode(
            encoder!!, pcmBuffer, frameSize, outputBuffer, maxPayloadSize
        )

        if (encodedBytes < 0) {
            println("Opus encode failed: $encodedBytes")
            return null
        }

        val result = ByteArray(encodedBytes)
        outputBuffer[result]
        return result
    }

    fun decode(opusData: ByteArray): ShortArray {
        val inputBuffer = ByteBuffer.allocateDirect(opusData.size)
        inputBuffer.put(opusData)
        inputBuffer.flip()

        val outputBuffer = ShortBuffer.allocate(frameSize)

        val decodedSamples = LibOpus.INSTANCE.opus_decode(
            decoder!!, inputBuffer, opusData.size, outputBuffer, frameSize, 0
        )

        if (decodedSamples < 0) {
            println("Opus decode failed: $decodedSamples")
            return ShortArray(frameSize)
        }

        return outputBuffer.array()
    }

    override fun close() {
        if (encoder != null) LibOpus.INSTANCE.opus_encoder_destroy(encoder!!)
        if (decoder != null) LibOpus.INSTANCE.opus_decoder_destroy(decoder!!)
    }
}