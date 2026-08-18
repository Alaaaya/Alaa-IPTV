package com.alaa.iptv.ui.player

import androidx.media3.common.PlaybackException

/** رسائل آمنة للمستخدم لا تعرض تفاصيل استثناء قد تتضمن رابط بث أو بيانات اعتماد. */
internal object PlayerErrorMessagePolicy {
    fun messageFor(errorCode: Int): String = when (errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "فشل الاتصال بالشبكة"
        PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE -> "نوع المحتوى غير مدعوم"
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "صيغة الملف غير مدعومة"
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "فشل تشغيل الفيديو (Decoder)"
        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "خطأ في استجابة الخادم"
        else -> "تعذر تشغيل البث"
    }
}
