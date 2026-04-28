package no.uio.ifi.in2000.team45.data.music

import android.content.Context
import android.media.MediaPlayer
import no.uio.ifi.in2000.team45.R

class MusicManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun updateMusicState(enabled: Boolean) {
        if (enabled) {
            startMusic()
        } else {
            stopMusic()
        }
    }

    private fun startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.glem_alt).apply {
                isLooping = true
                start()
            }
        } else if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    private fun stopMusic() {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}