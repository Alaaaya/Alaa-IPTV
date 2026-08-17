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
    const val LIVE_FAVORITES = "live_favorites"
    const val LIVE_CHANNEL_MOVE = "live_channel_move"
    const val LIVE_NUMBER_JUMP = "live_number_jump"
    const val LIVE_AUDIO_ONLY = "live_audio_only"
    const val PLAYER_AUDIO_TRACKS = "player_audio_tracks"
    const val PLAYER_SUBTITLES = "player_subtitles"
    const val PLAYER_BACKGROUND_AUDIO = "player_background_audio"
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
    const val IMAGE_CACHE_CLEAR = "image_cache_clear"
    const val DATA_SAVER = "data_saver"
    const val LOW_BANDWIDTH_POSTERS = "low_bandwidth_posters"
    const val EYE_COMFORT = "eye_comfort"
    const val LARGE_TEXT = "large_text"
    const val ROOMY_POSTERS = "roomy_posters"
    const val GUEST_MODE = "guest_mode"
    const val SLEEP_TIMER = "sleep_timer"
    const val SETTINGS_LOCK = "settings_lock"
    const val HIDE_DIAGNOSTICS = "hide_diagnostics"
    const val PERMISSIONS_INFO = "permissions_info"
    const val LONG_NOTIFICATIONS = "long_notifications"
    const val NAVIGATION_SOUNDS = "navigation_sounds"
    const val IDLE_REMINDER = "idle_reminder"
    const val AUTO_CACHE_CLEAN = "auto_cache_clean"
    const val COMPATIBILITY_MODE = "compatibility_mode"
    const val HIGH_PERFORMANCE_MODE = "high_performance_mode"
    const val LOW_LATENCY_MODE = "low_latency_mode"
    const val STORAGE_WARNING = "storage_warning"
    const val REMOTE_TEST = "remote_test"
    const val REMOTE_GUIDE = "remote_guide"
    const val RESET_PREFERENCES = "reset_preferences"
    const val SAFE_SUPPORT_REPORT = "safe_support_report"
    const val WHATS_NEW = "whats_new"

    val options = listOf(
        OptionalFeature(GLOBAL_SEARCH, "البحث الشامل", "ابحث في القنوات والأفلام والمسلسلات.", "المكتبة"),
        OptionalFeature(WATCHLIST, "المشاهدة لاحقاً", "احفظ الأفلام والمسلسلات التي تريد الرجوع إليها.", "المكتبة"),
        OptionalFeature(WATCH_HISTORY, "سجل المشاهدة", "اعرض آخر ما شُغّل ضمن الملف الحالي.", "المكتبة"),
        OptionalFeature(RESUME_PLAYBACK, "استئناف المشاهدة", "تابع الفيلم أو الحلقة من آخر موضع.", "المكتبة"),
        OptionalFeature(RECENT_CHANNELS, "القنوات الحديثة", "أظهر آخر القنوات المفتوحة للعودة السريعة.", "المكتبة"),
        OptionalFeature(LIVE_FAVORITES, "قنوات مفضلة", "اسمح بإضافة القنوات إلى قائمة المفضلة المحلية.", "القنوات المباشرة", defaultEnabled = false),
        OptionalFeature(LIVE_CHANNEL_MOVE, "نقل القنوات", "اسمح بإعادة ترتيب القنوات محلياً من خيارات القناة.", "القنوات المباشرة", defaultEnabled = false),
        OptionalFeature(LIVE_NUMBER_JUMP, "الانتقال برقم القناة", "اسمح بإدخال رقم قناة من الريموت للانتقال إليها داخل الفئة المحملة.", "القنوات المباشرة", defaultEnabled = false),
        OptionalFeature(LIVE_AUDIO_ONLY, "بث صوت فقط", "عطّل مسار الفيديو للبث المباشر واستخدم الصوت فقط عند توفره لتقليل الاستهلاك.", "القنوات المباشرة", defaultEnabled = false),
        OptionalFeature(PLAYER_AUDIO_TRACKS, "اختيار مسار الصوت", "أظهر مسارات اللغة أو الدبلجة المتاحة في الفيديو عند توفيرها من المصدر.", "التشغيل", defaultEnabled = false),
        OptionalFeature(PLAYER_SUBTITLES, "اختيار الترجمة", "أظهر الترجمات المتاحة والسماح بإيقافها أثناء تشغيل الفيديو.", "التشغيل", defaultEnabled = false),
        OptionalFeature(PLAYER_BACKGROUND_AUDIO, "الصوت في الخلفية", "استمر في تشغيل الصوت عند انتقال التطبيق إلى الخلفية مؤقتاً؛ يتوقف عند إغلاق المشغل.", "التشغيل", defaultEnabled = false),
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
        OptionalFeature(REMOTE_CONFIG_CONFIRMATION, "تأكيد الإعدادات البعيدة", "أظهر إشعاراً عند تطبيق إعدادات مركزية جديدة.", "الاتصال", defaultEnabled = false),
        OptionalFeature(IMAGE_CACHE_CLEAR, "تنظيف الصور المؤقتة", "اسمح بمسح صور البوسترات المؤقتة عند الحاجة.", "الاتصال", defaultEnabled = false),
        OptionalFeature(DATA_SAVER, "وضع اقتصاد البيانات", "خفّض دقة تحميل صور البوسترات عند التصفح.", "الأداء والبيانات", defaultEnabled = false),
        OptionalFeature(LOW_BANDWIDTH_POSTERS, "صور أخف للشبكة الضعيفة", "استخدم نسخة أصغر من صور البوسترات لتسريع التصفح.", "الأداء والبيانات", defaultEnabled = false),
        OptionalFeature(EYE_COMFORT, "راحة العين", "خفف تباين الخلفيات ليلاً دون تغيير تصميمك المختار.", "المظهر", defaultEnabled = false),
        OptionalFeature(LARGE_TEXT, "نص أكبر", "كبّر النصوص الأساسية لتكون أوضح من مسافة بعيدة.", "المظهر", defaultEnabled = false),
        OptionalFeature(ROOMY_POSTERS, "بوسترات أكبر", "قلل عدد البطاقات في الصف لتظهر بوسترات المكتبة بشكل أوضح.", "المظهر", defaultEnabled = false),
        OptionalFeature(GUEST_MODE, "وضع ضيف", "لا تحفظ سجل المشاهدة أو القنوات الحديثة أثناء تفعيله.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(SLEEP_TIMER, "مؤقت النوم", "اسمح بإيقاف التشغيل تلقائياً بعد مدة تختارها داخل المشغل.", "التشغيل", defaultEnabled = false),
        OptionalFeature(SETTINGS_LOCK, "قفل الإعدادات", "اطلب رمز ملف المالك قبل فتح الإعدادات عند وجوده.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(HIDE_DIAGNOSTICS, "إخفاء معلومات التشخيص", "أخفِ TV ID وأدوات التشخيص من العرض العادي.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(PERMISSIONS_INFO, "شرح الأذونات", "اعرض شرحاً محلياً لسبب استخدام أذونات التطبيق.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(LONG_NOTIFICATIONS, "تنبيهات أطول", "أطِل مدة رسائل الحالة لتكون أوضح من مسافة بعيدة.", "المظهر", defaultEnabled = false),
        OptionalFeature(NAVIGATION_SOUNDS, "أصوات التنقل", "فعّل أصوات Android TV الخفيفة عند الضغط والتنقل.", "المظهر", defaultEnabled = false),
        OptionalFeature(IDLE_REMINDER, "تذكير عدم النشاط", "نبّهك قبل إيقاف التشغيل المحلي بعد فترة طويلة بلا تفاعل.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(AUTO_CACHE_CLEAN, "تنظيف مؤقت تلقائي", "نظّف صور البوسترات القديمة دورياً في الخلفية.", "الأداء والبيانات", defaultEnabled = false),
        OptionalFeature(COMPATIBILITY_MODE, "وضع توافق للأجهزة الضعيفة", "استخدم مخزناً مؤقتاً أكثر تحفظاً لتقليل التقطيع على الأجهزة الضعيفة.", "الأداء والبيانات", defaultEnabled = false),
        OptionalFeature(HIGH_PERFORMANCE_MODE, "وضع أداء عالٍ", "استخدم مخزناً مؤقتاً أسرع على الأجهزة القوية؛ أوقف وضع التوافق أولاً.", "الأداء والبيانات", defaultEnabled = false),
        OptionalFeature(LOW_LATENCY_MODE, "وضع زمن استجابة منخفض", "يقلل التخزين المؤقت في البث المباشر لتقريب البث من المصدر؛ قد لا يناسب الشبكات غير المستقرة.", "الأداء والبيانات", defaultEnabled = false),
        OptionalFeature(STORAGE_WARNING, "تنبيه مساحة التخزين", "نبّهك عندما تقل المساحة المتاحة عن حد آمن.", "الأداء والبيانات", defaultEnabled = false),
        OptionalFeature(REMOTE_TEST, "اختبار الريموت", "اعرض آخر زر يصل إلى التطبيق للتحقق من الريموت.", "التشغيل", defaultEnabled = false),
        OptionalFeature(REMOTE_GUIDE, "دليل اختصارات الريموت", "اعرض ملخص اختصارات التنقل والتشغيل داخل التطبيق.", "التشغيل", defaultEnabled = false),
        OptionalFeature(RESET_PREFERENCES, "استعادة إعدادات العرض", "أعد التصميم والخيارات المحلية إلى افتراضياتها دون حذف الاشتراك.", "الخصوصية", defaultEnabled = false),
        OptionalFeature(SAFE_SUPPORT_REPORT, "تقرير دعم آمن", "انسخ تقريراً قصيراً بلا بيانات اشتراك للمشاركة مع الدعم.", "الاتصال", defaultEnabled = false),
        OptionalFeature(WHATS_NEW, "ما الجديد", "اعرض ملخصاً محلياً للإضافات بعد تحديث التطبيق.", "الواجهة", defaultEnabled = false)
    )

    fun option(id: String): OptionalFeature = options.firstOrNull { it.id == id }
        ?: error("Unknown optional feature: $id")
}
