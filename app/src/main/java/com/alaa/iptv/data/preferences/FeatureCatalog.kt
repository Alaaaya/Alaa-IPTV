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
    const val QUICK_START = "quick_start"
    const val START_SCREEN = "start_screen"
    const val CONTENT_RELOAD = "content_reload"
    const val SESSION_RECOVERY = "session_recovery"
    const val SERVICE_STATUS = "service_status"
    const val SAFE_ERROR_LOG = "safe_error_log"
    const val CONNECTION_REFERENCE = "connection_reference"
    const val NETWORK_DIAGNOSTICS = "network_diagnostics"
    const val UNIFIED_LOADING = "unified_loading"
    const val SMART_EMPTY_STATES = "smart_empty_states"
    const val RECOVERY_ACTIONS = "recovery_actions"
    const val TV_ID_SHORTCUT = "tv_id_shortcut"
    const val SYNC_STATUS = "sync_status"
    const val MANUAL_SYNC = "manual_sync"
    const val REMOTE_CONFIG_CONFIRMATION = "remote_config_confirmation"

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
        OptionalFeature(OWNER_ALERTS, "تنبيهات لوحة التحكم", "أرسل تنبيهات اختيارية للمالك عن الجهاز والاتصال.", "الاتصال", defaultEnabled = false),
        OptionalFeature(QUICK_START, "بدء سريع", "افتح آخر قناة أو آخر شاشة حسب تفضيلك.", "التشغيل", defaultEnabled = false),
        OptionalFeature(START_SCREEN, "شاشة بدء مخصصة", "اختر فتح الرئيسية أو آخر شاشة محفوظة.", "التشغيل", defaultEnabled = false),
        OptionalFeature(CONTENT_RELOAD, "إعادة تحميل المحتوى", "أظهر زر إعادة التحميل عند الحاجة دون إغلاق التطبيق.", "الواجهة"),
        OptionalFeature(SESSION_RECOVERY, "استرداد الجلسة", "أعدك إلى تسجيل الدخول برسالة واضحة عند انتهاء الحساب.", "الاتصال"),
        OptionalFeature(SERVICE_STATUS, "حالة الخدمة", "أظهر حالة اتصال التطبيق والخادم عند الطلب.", "الاتصال", defaultEnabled = false),
        OptionalFeature(SAFE_ERROR_LOG, "سجل أخطاء آمن", "احفظ آخر الأخطاء دون كلمات مرور أو روابط خاصة.", "الاتصال", defaultEnabled = false),
        OptionalFeature(CONNECTION_REFERENCE, "رقم مرجعي للخطأ", "أظهر رمزاً قصيراً عند حدوث خطأ لتسهيل الدعم.", "الاتصال", defaultEnabled = false),
        OptionalFeature(NETWORK_DIAGNOSTICS, "تشخيص الشبكة", "افحص اتصال الإنترنت والخادم يدوياً عند الحاجة.", "الاتصال", defaultEnabled = false),
        OptionalFeature(UNIFIED_LOADING, "مؤشر تحميل موحد", "أظهر حالة تحميل هادئة وواضحة في القوائم.", "الواجهة"),
        OptionalFeature(SMART_EMPTY_STATES, "حالات فارغة ذكية", "اشرح سبب عدم وجود نتائج واقترح إعادة التحميل.", "الواجهة"),
        OptionalFeature(RECOVERY_ACTIONS, "خيارات استرداد", "اعرض إعادة المحاولة أو إعادة التحميل عند فشل الصورة أو الفيديو.", "الواجهة"),
        OptionalFeature(TV_ID_SHORTCUT, "اختصار TV ID", "أظهر معرف التلفزيون في الإعدادات لتسهيل الدعم.", "الاتصال"),
        OptionalFeature(SYNC_STATUS, "حالة المزامنة", "أظهر وقت آخر مزامنة مع لوحة الإدارة.", "الاتصال", defaultEnabled = false),
        OptionalFeature(MANUAL_SYNC, "مزامنة فورية", "اسمح بطلب مزامنة يدوية مع لوحة الإدارة.", "الاتصال", defaultEnabled = false),
        OptionalFeature(REMOTE_CONFIG_CONFIRMATION, "تأكيد الإعدادات البعيدة", "أظهر إشعاراً عند تطبيق إعدادات مركزية جديدة.", "الاتصال", defaultEnabled = false)
    )

    fun option(id: String): OptionalFeature = options.firstOrNull { it.id == id }
        ?: error("Unknown optional feature: $id")
}
