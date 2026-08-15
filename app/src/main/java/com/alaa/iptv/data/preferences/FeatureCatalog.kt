package com.alaa.iptv.data.preferences

data class OptionalFeature(
    val id: String,
    val title: String,
    val description: String,
    val section: String,
    val defaultEnabled: Boolean = true
)

/** قائمة الميزات التي يختار مالك الجهاز تشغيلها من الإعدادات. */
object FeatureCatalog {
    const val GLOBAL_SEARCH = "global_search"
    const val WATCHLIST = "watchlist"
    const val WATCH_HISTORY = "watch_history"
    const val RESUME_PLAYBACK = "resume_playback"
    const val AUTO_NEXT_EPISODE = "auto_next_episode"
    const val SKIP_CONTROLS = "skip_controls"
    const val PARENTAL_PIN = "parental_pin"
    const val PROFILES = "profiles"
    const val HIDE_CONTENT = "hide_content"
    const val CATEGORY_ORDER = "category_order"
    const val MULTI_SUBSCRIPTIONS = "multi_subscriptions"
    const val CONNECTION_TEST = "connection_test"
    const val QUALITY_SELECTION = "quality_selection"
    const val AUTO_RECONNECT = "auto_reconnect"
    const val REMOTE_SHORTCUTS = "remote_shortcuts"
    const val FOCUS_PREVIEW = "focus_preview"
    const val LIBRARY_FILTERS = "library_filters"
    const val ENCRYPTED_BACKUP = "encrypted_backup"
    const val OWNER_ALERTS = "owner_alerts"
    const val SIMPLE_MODE = "simple_mode"
    const val HOME_CUSTOMIZATION = "home_customization"
    const val RECENT_CHANNELS = "recent_channels"

    val options = listOf(
        OptionalFeature(GLOBAL_SEARCH, "البحث الشامل", "ابحث في القنوات والأفلام والمسلسلات.", "المكتبة"),
        OptionalFeature(WATCHLIST, "المشاهدة لاحقاً", "احفظ الأفلام والمسلسلات التي تريد الرجوع إليها.", "المكتبة"),
        OptionalFeature(WATCH_HISTORY, "سجل المشاهدة", "اعرض آخر ما شُغّل ضمن الملف الحالي.", "المكتبة"),
        OptionalFeature(RESUME_PLAYBACK, "استئناف المشاهدة", "تابع الفيلم أو الحلقة من آخر موضع.", "المكتبة"),
        OptionalFeature(RECENT_CHANNELS, "القنوات الحديثة", "أظهر آخر القنوات المفتوحة للعودة السريعة.", "المكتبة"),
        OptionalFeature(AUTO_NEXT_EPISODE, "الحلقة التالية تلقائياً", "اعرض عدّاداً وشغّل الحلقة التالية عند انتهاء الحالية.", "التشغيل"),
        OptionalFeature(SKIP_CONTROLS, "أزرار التخطي", "التقديم أو الإرجاع 10 و30 ثانية داخل الفيديو.", "التشغيل"),
        OptionalFeature(QUALITY_SELECTION, "اختيار جودة البث", "اسمح باختيار الجودة عندما يوفرها الاشتراك.", "التشغيل"),
        OptionalFeature(AUTO_RECONNECT, "إعادة الاتصال", "أعد محاولة البث تلقائياً عند انقطاعه.", "التشغيل"),
        OptionalFeature(REMOTE_SHORTCUTS, "اختصارات الريموت", "فعّل اختصارات الأزرار الملوّنة القابلة للتخصيص.", "التشغيل"),
        OptionalFeature(FOCUS_PREVIEW, "معاينة عند التركيز", "أظهر بيانات موسعة عند الوقوف على فيلم أو مسلسل.", "الواجهة"),
        OptionalFeature(LIBRARY_FILTERS, "فلترة المكتبة", "فعّل فلاتر النوع والسنة والجودة واللغة.", "الواجهة"),
        OptionalFeature(HIDE_CONTENT, "إخفاء محتوى", "أخفِ فئات أو قنوات لا تريد ظهورها.", "الواجهة"),
        OptionalFeature(CATEGORY_ORDER, "ترتيب الفئات", "رتّب الفئات وثبّت الأهم في الأعلى.", "الواجهة"),
        OptionalFeature(HOME_CUSTOMIZATION, "تخصيص الرئيسية", "اختر الأقسام وترتيبها في الصفحة الرئيسية.", "الواجهة"),
        OptionalFeature(SIMPLE_MODE, "وضع مبسّط", "واجهة مختصرة للمشاهدة السهلة.", "الواجهة", defaultEnabled = false),
        OptionalFeature(PARENTAL_PIN, "قفل PIN", "احمِ محتوى أو فئات محددة برمز سري.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(PROFILES, "ملفات تعريف", "افصل الإعدادات وسجل المشاهدة للكبار والأطفال.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(MULTI_SUBSCRIPTIONS, "اشتراكات متعددة", "احفظ أكثر من اشتراك IPTV وانتقل بينها.", "الاتصال", defaultEnabled = false),
        OptionalFeature(CONNECTION_TEST, "فحص الاشتراك", "اختبر بيانات الخادم والاتصال قبل التحميل.", "الاتصال"),
        OptionalFeature(ENCRYPTED_BACKUP, "نسخ احتياطي مشفّر", "صدّر أو استورد التفضيلات دون كلمات المرور.", "الاتصال", defaultEnabled = false),
        OptionalFeature(OWNER_ALERTS, "تنبيهات لوحة التحكم", "أرسل تنبيهات اختيارية للمالك عن الجهاز والاتصال.", "الاتصال", defaultEnabled = false)
    )

    fun option(id: String): OptionalFeature = options.firstOrNull { it.id == id }
        ?: error("Unknown optional feature: $id")
}
