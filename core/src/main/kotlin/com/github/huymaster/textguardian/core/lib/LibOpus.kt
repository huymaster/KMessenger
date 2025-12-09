package com.github.huymaster.textguardian.core.lib

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import java.nio.ByteBuffer
import java.nio.ShortBuffer

interface LibOpus : Library {
    companion object {
        val INSTANCE: LibOpus by lazy { Native.load("libopus", LibOpus::class.java) }

        const val OPUS_APPLICATION_VOIP = 2048
        const val OPUS_OK = 0
    }

    fun opus_encoder_create(Fs: Int, channels: Int, application: Int, error: IntByReference?): Pointer?

    fun opus_encode(st: Pointer, pcm: ShortBuffer, frame_size: Int, data: ByteBuffer, max_data_bytes: Int): Int

    fun opus_encoder_destroy(st: Pointer)

    fun opus_decoder_create(Fs: Int, channels: Int, error: IntByReference?): Pointer?

    fun opus_decode(st: Pointer, data: ByteBuffer?, len: Int, pcm: ShortBuffer, frame_size: Int, decode_fec: Int): Int

    fun opus_decoder_destroy(st: Pointer)
}