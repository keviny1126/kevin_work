package com.power.baseproject.utils

import android.content.Context.AUDIO_SERVICE
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.power.baseproject.R
import com.power.baseproject.ktbase.application.BaseApplication
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SoundUtils {
    private lateinit var soundPool: SoundPool
    private var clickLoadSound: Int = -1
    private var testLoadSound: Int = -1
    private var clickPlaySound: Int = -1
    private var needPlay = false

    fun init() {
        setVolume(100)
        val abs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(2) //设置允许同时播放的流的最大值
            .setAudioAttributes(abs)
            .build()
        clickLoadSound = soundPool.load(BaseApplication.getContext(), R.raw.click, 1)
        testLoadSound = soundPool.load(BaseApplication.getContext(), R.raw.test_success, 1)
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            LogUtil.i("kevin", "《============按键音频加载完成=============》status:$status")
            if (status == 0) {
                needPlay = true
            }
        }

    }

    fun playClickSound() {
        if (needPlay) {
            clickPlaySound = soundPool.play(clickLoadSound, 1f, 1f, 1, 0, 1f)
            LogUtil.i(
                "kevin",
                "《============点击按键音=============》clickLoadSound：${clickLoadSound} ---clickPlaySound:$clickPlaySound"
            )
        }
    }

    fun stopClickSound() {
        soundPool.stop(clickPlaySound)
        clickPlaySound = -1
    }

    suspend fun playSound(time: Long): Boolean {
        return withContext(Dispatchers.IO) {
            if (needPlay) {
                clickPlaySound = soundPool.play(testLoadSound, 1f, 1f, 1, -1, 1f)
                delay(time)
                soundPool.stop(clickPlaySound)
                clickPlaySound = -1
                true
            } else {
                false
            }
        }
    }

    fun release() {
        soundPool.release()
    }

    fun setVolume(progress: Int) {
        val am = BaseApplication.getContext()
            .getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume1 = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val maxVolume2 = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
        val maxVolume3 = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val maxVolume4 = am.getStreamMaxVolume(AudioManager.STREAM_RING)
        val maxVolume5 = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        val maxVolume6 = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        val maxVolume7 = am.getStreamMaxVolume(AudioManager.STREAM_DTMF)
        val volume1: Int
        val volume2: Int
        val volume3: Int
        val volume4: Int
        val volume5: Int
        val volume6: Int
        val volume7: Int
        when (progress) {
            0 -> {
                volume1 = 0
                volume2 = 1
                volume3 = 0
                volume4 = 1
                volume5 = 0
                volume6 = 1
                volume7 = 0
            }
            100 -> {
                volume1 = maxVolume1
                volume2 = maxVolume2
                volume3 = maxVolume3
                volume4 = maxVolume4
                volume5 = maxVolume5
                volume6 = maxVolume6
                volume7 = maxVolume7
            }
            else -> {
                volume1 = (2 * progress + 100) * maxVolume1 / 300
                volume2 = (2 * progress + 100) * maxVolume2 / 300
                volume3 = (2 * progress + 100) * maxVolume3 / 300
                volume4 = (2 * progress + 100) * maxVolume4 / 300
                volume5 = (2 * progress + 100) * maxVolume5 / 300
                volume6 = (2 * progress + 100) * maxVolume6 / 300
                volume7 = (2 * progress + 100) * maxVolume7 / 300
            }
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume1, 0) //tempVolume:音量绝对值
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, volume2, 0) //tempVolume:音量绝对值
        am.setStreamVolume(AudioManager.STREAM_ALARM, volume3, 0) //tempVolume:音量绝对值
        am.setStreamVolume(AudioManager.STREAM_RING, volume4, 0) //tempVolume:音量绝对值
        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume5, 0) //tempVolume:音量绝对值
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume6, 0) //tempVolume:音量绝对值
        am.setStreamVolume(AudioManager.STREAM_DTMF, volume7, 0) //tempVolume:音量绝对值
    }

    companion object {
        val instance: SoundUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SoundUtils()
        }
    }
}