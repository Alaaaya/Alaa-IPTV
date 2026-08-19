package com.alaa.iptv.ui.common

import android.animation.ValueAnimator
import android.content.ContentResolver
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import com.alaa.iptv.R
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * إطار تركيز موحد لعناصر Android TV. يرسم فوق العنصر ولا يغيّر تخطيط الشاشة.
 * الإطار ثابت، أما التوهج فينبض بهدوء ويمكن إيقافه من إعدادات Android للحركة.
 */
object PremiumNeonFocus {
    private const val CRIMSON = 0xFFE53935.toInt()

    fun install(root: View) {
        if (root.getTag(R.id.premium_neon_focus_tracker) != null) return
        val tracker = FocusTracker(root)
        root.setTag(R.id.premium_neon_focus_tracker, tracker)
        root.viewTreeObserver.addOnGlobalFocusChangeListener(tracker)
        root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit

            override fun onViewDetachedFromWindow(v: View) {
                if (v.viewTreeObserver.isAlive) {
                    v.viewTreeObserver.removeOnGlobalFocusChangeListener(tracker)
                }
                tracker.clear()
                v.setTag(R.id.premium_neon_focus_tracker, null)
                v.removeOnAttachStateChangeListener(this)
            }
        })
    }

    private class FocusTracker(root: View) : ViewTreeFocusListener {
        private val rootReference = WeakReference(root)
        private val visuals = WeakHashMap<View, NeonVisual>()

        override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
            oldFocus?.takeIf { isEligible(it) }?.let { view ->
                visuals.remove(view)?.lose()
            }
            newFocus?.takeIf { isEligible(it) }?.let { view ->
                visuals.getOrPut(view) { NeonVisual(view) }.gain()
            }
        }

        fun clear() {
            visuals.values.forEach { it.removeImmediately() }
            visuals.clear()
        }

        private fun isEligible(view: View): Boolean {
            val root = rootReference.get() ?: return false
            if (view === root || !view.isFocusable || view.visibility != View.VISIBLE) return false
            if (view.width <= 0 || view.height <= 0) return false
            return isDescendantOf(view, root)
        }

        private fun isDescendantOf(view: View, root: View): Boolean {
            var current: View? = view
            while (current != null && current !== root) {
                current = current.parent as? View
            }
            return current === root
        }
    }

    private interface ViewTreeFocusListener : ViewTreeObserver.OnGlobalFocusChangeListener

    private class NeonVisual(private val target: View) {
        private val frame = NeonFrameDrawable(target.resources.displayMetrics.density)
        private var pulseAnimator: ValueAnimator? = null
        private var fadeAnimator: ValueAnimator? = null
        private var attached = false

        fun gain() {
            fadeAnimator?.let { animator ->
                fadeAnimator = null
                animator.cancel()
            }
            attachIfNeeded()
            target.post {
                if (!attached) return@post
                updateBounds()
                val animatorScale = animatorScale(target.context.contentResolver)
                val duration = PremiumNeonFocusMotionPolicy.scaledDuration(
                    PremiumNeonFocusMotionPolicy.FOCUS_IN_DURATION_MS,
                    animatorScale
                )
                target.animate().cancel()
                if (duration == 0L) {
                    target.scaleX = PremiumNeonFocusMotionPolicy.FOCUSED_SCALE
                    target.scaleY = PremiumNeonFocusMotionPolicy.FOCUSED_SCALE
                    frame.setGlow(0.58f)
                    return@post
                }
                target.scaleX = 0.992f
                target.scaleY = 0.992f
                target.animate()
                    .scaleX(PremiumNeonFocusMotionPolicy.FOCUSED_SCALE)
                    .scaleY(PremiumNeonFocusMotionPolicy.FOCUSED_SCALE)
                    .setDuration(duration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                startPulse(animatorScale)
            }
        }

        fun lose() {
            pulseAnimator?.let { animator ->
                pulseAnimator = null
                animator.cancel()
            }
            val animatorScale = animatorScale(target.context.contentResolver)
            val duration = PremiumNeonFocusMotionPolicy.scaledDuration(
                PremiumNeonFocusMotionPolicy.FOCUS_OUT_DURATION_MS,
                animatorScale
            )
            target.animate().cancel()
            target.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
            if (duration == 0L) {
                removeImmediately()
                return
            }
            val startGlow = frame.glowStrength
            lateinit var animator: ValueAnimator
            animator = ValueAnimator.ofFloat(startGlow, 0f).apply {
                this.duration = duration
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    frame.setGlow(it.animatedValue as Float)
                    target.invalidate()
                }
                doOnEnd {
                    if (fadeAnimator === animator) {
                        fadeAnimator = null
                        removeImmediately()
                    }
                }
            }
            fadeAnimator = animator
            animator.start()
        }

        fun removeImmediately() {
            pulseAnimator?.let { animator ->
                pulseAnimator = null
                animator.cancel()
            }
            fadeAnimator?.let { animator ->
                fadeAnimator = null
                animator.cancel()
            }
            target.animate().cancel()
            target.scaleX = 1f
            target.scaleY = 1f
            if (attached) target.overlay.remove(frame)
            attached = false
        }

        private fun attachIfNeeded() {
            if (attached) return
            target.overlay.add(frame)
            attached = true
        }

        private fun updateBounds() {
            val inset = (12 * target.resources.displayMetrics.density).toInt()
            frame.bounds = Rect(-inset, -inset, target.width + inset, target.height + inset)
        }

        private fun startPulse(animatorScale: Float) {
            pulseAnimator?.let { animator ->
                pulseAnimator = null
                animator.cancel()
            }
            if (animatorScale <= 0f) {
                frame.setGlow(0.58f)
                return
            }
            val duration = PremiumNeonFocusMotionPolicy.scaledDuration(
                PremiumNeonFocusMotionPolicy.PULSE_DURATION_MS,
                animatorScale
            )
            pulseAnimator = ValueAnimator.ofFloat(0.34f, 0.78f).apply {
                this.duration = duration
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    frame.setGlow(it.animatedValue as Float)
                    target.invalidate()
                }
                start()
            }
        }
    }

    private class NeonFrameDrawable(private val density: Float) : Drawable() {
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        private val outerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val innerGlow = Paint(Paint.ANTI_ALIAS_FLAG)
        private val outline = Paint(Paint.ANTI_ALIAS_FLAG)
        private val accent = Paint(Paint.ANTI_ALIAS_FLAG)
        var glowStrength: Float = 0.58f
            private set

        init {
            fill.style = Paint.Style.FILL
            outerGlowPaint.style = Paint.Style.STROKE
            innerGlow.style = Paint.Style.STROKE
            outline.style = Paint.Style.STROKE
            accent.style = Paint.Style.FILL
        }

        fun setGlow(value: Float) {
            glowStrength = value.coerceIn(0f, 1f)
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            val rect = bounds
            if (rect.isEmpty) return
            val radius = 10f * density
            val inset = 8f * density
            val left = rect.left + inset
            val top = rect.top + inset
            val right = rect.right - inset
            val bottom = rect.bottom - inset

            fill.color = Color.argb((26 + 18 * glowStrength).toInt(), 112, 10, 22)
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, fill)

            outerGlowPaint.color = Color.argb((20 + 52 * glowStrength).toInt(), 229, 57, 53)
            outerGlowPaint.strokeWidth = (10f + 7f * glowStrength) * density
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, outerGlowPaint)

            innerGlow.color = Color.argb((44 + 48 * glowStrength).toInt(), 255, 84, 80)
            innerGlow.strokeWidth = (4f + 2f * glowStrength) * density
            canvas.drawRoundRect(left + 2 * density, top + 2 * density, right - 2 * density, bottom - 2 * density, radius - 2 * density, radius - 2 * density, innerGlow)

            outline.color = CRIMSON
            outline.alpha = 255
            outline.strokeWidth = 1.5f * density
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, outline)

            accent.color = CRIMSON
            accent.alpha = (180 + 60 * glowStrength).toInt()
            canvas.drawRoundRect(left + 5 * density, top + 6 * density, left + 9 * density, bottom - 6 * density, 3 * density, 3 * density, accent)
        }

        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) = Unit
        override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
    }

    private fun animatorScale(contentResolver: ContentResolver): Float = runCatching {
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    }.getOrDefault(1f)

    private fun ValueAnimator.doOnEnd(block: () -> Unit) {
        addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) = block()
        })
    }
}
