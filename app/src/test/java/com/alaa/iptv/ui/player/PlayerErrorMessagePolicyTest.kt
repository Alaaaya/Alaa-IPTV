package com.alaa.iptv.ui.player

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerErrorMessagePolicyTest {
    @Test
    fun `HTTP and unknown playback failures use safe messages without endpoint details`() {
        assertEquals(
            "خطأ في استجابة الخادم",
            PlayerErrorMessagePolicy.messageFor(PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS)
        )
        assertEquals(
            "تعذر تشغيل البث",
            PlayerErrorMessagePolicy.messageFor(PlaybackException.ERROR_CODE_UNSPECIFIED)
        )
    }

    @Test
    fun `network failure is explained without exposing a stream URL`() {
        assertEquals(
            "فشل الاتصال بالشبكة",
            PlayerErrorMessagePolicy.messageFor(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
        )
    }
}
