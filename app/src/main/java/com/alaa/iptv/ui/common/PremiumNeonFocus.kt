package com.alaa.iptv.ui.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver
import com.alaa.iptv.R
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/** إطار تركيز ثابت وبسيط لعناصر Android TV، من دون توهج أو نبض أو تكبير. */
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
        private val visuals = WeakHashMap<View, StaticFocusVisual>()

        override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
            oldFocus?.takeIf { isEligible(it) }?.let { view -> visuals.remove(view)?.remove() }
            newFocus?.takeIf { isEligible(it) }?.let { view ->
                visuals.getOrPut(view) { StaticFocusVisual(view) }.show()
            }
        }

        fun clear() {
            visuals.values.forEach { it.remove() }
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
            while (current != null && current !== root) current = current.parent as? View
            return current === root
        }
    }

    private interface ViewTreeFocusListener : ViewTreeObserver.OnGlobalFocusChangeListener

    private class StaticFocusVisual(private val target: View) {
        private val frame = StaticFocusFrameDrawable(target.resources.displayMetrics.density)
        private var attached = false

        fun show() {
            if (!attached) {
                target.overlay.add(frame)
                attached = true
            }
            target.post {
                if (!attached) return@post
                val inset = (5 * target.resources.displayMetrics.density).toInt()
                frame.bounds = Rect(-inset, -inset, target.width + inset, target.height + inset)
                target.invalidate()
            }
        }

        fun remove() {
            if (attached) target.overlay.remove(frame)
            attached = false
            target.scaleX = 1f
            target.scaleY = 1f
            target.translationZ = 0f
        }
    }

    private class StaticFocusFrameDrawable(private val density: Float) : Drawable() {
        private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = CRIMSON
            strokeWidth = 2f * density
        }

        override fun draw(canvas: Canvas) {
            val rect = bounds
            if (rect.isEmpty) return
            val inset = 3f * density
            canvas.drawRoundRect(
                rect.left + inset,
                rect.top + inset,
                rect.right - inset,
                rect.bottom - inset,
                8f * density,
                8f * density,
                outline
            )
        }

        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) = Unit
        override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
    }
}
