package com.experiment.jetpackxr.soundexplorer.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.PointSourceAttributes
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.SpatialAudioTrackBuilder
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class SoundManager @Inject constructor() : Closeable {

    private var audioTracks: MutableList<AudioTrack> = mutableListOf()

    private fun parseWAVFile(session: Session, soundResourceId: Int): ByteArray? {
        var inputStream: InputStream? = null
        try {
            inputStream = session.activity.resources.openRawResource(soundResourceId)

            // wav file header checks
            val riffId = String(inputStream.readNBytes(4))
            if (riffId != "RIFF") throw IOException("Invalid WAV file - Missing 'RIFF'")
            val waveId = String(inputStream.readNBytes(8), 4, 4)
            if (waveId != "WAVE") throw IOException("Invalid WAV file - Missing 'WAVE'")

            var dataId = String(inputStream.readNBytes(4))
            while (dataId != "data") {
                val nextChar = String(inputStream.readNBytes(1))
                if (nextChar.length != 1) {
                    throw IOException("Invalid WAV file - No data section found!")
                }
                dataId = dataId.substring(1) + nextChar
            }

            val bufferSize = ByteBuffer
                .wrap(inputStream.readNBytes(4))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt(0)

            return inputStream.readNBytes(bufferSize)
        } finally {
            inputStream?.close()
        }

        return null
    }

    fun loadSound(session: Session, entity: Entity, soundResourceId: Int): Int? {
        var audioTrackInitialized = false
        var audioTrack: AudioTrack? = null

        try {
            val audioBuffer = parseWAVFile(session, soundResourceId)

            if (audioBuffer == null) {
                return null
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormatConfig = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrackBuilder = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormatConfig)
                .setBufferSizeInBytes(audioBuffer.size)
                .setTransferMode(AudioTrack.MODE_STATIC)

            val pointSourceAttributes = PointSourceAttributes(entity)

            audioTrack = SpatialAudioTrackBuilder
                .setPointSourceAttributes(
                    session,
                    audioTrackBuilder,
                    pointSourceAttributes
                )
                .build()

            audioTrack.write(audioBuffer, 0, audioBuffer.size)

            audioTrack.setLoopPoints(0, audioTrack.bufferSizeInFrames, -1)
            audioTrack.setVolume(0.0f)

            this.audioTracks.add(audioTrack)

            audioTrackInitialized = true
        } finally {
            if (audioTrack != null && !audioTrackInitialized && audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.release()
            }
        }

        return this.audioTracks.size - 1
    }

    // Note - playing all sounds in order seems to execute faster than starting sounds in an
    // arbitrary order. Measure and exercise caution if you consider refactoring this call to play
    // individual sounds instead of all of them.
    fun playAllSounds() {
        if (this.audioTracks.isEmpty()) {
            return
        }

        // val st = System.nanoTime()
        this.audioTracks[0].play()
        for (i in 1..<this.audioTracks.size) {
            this.audioTracks[i].play()
        }
    }

    fun setVolume(soundId: Int, volume: Float) {
        this.audioTracks[soundId].setVolume(volume)
    }

    override fun close() {
        for (audioTrack in audioTracks) {
            if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop()
            }
            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.release()
            }
        }
    }
}