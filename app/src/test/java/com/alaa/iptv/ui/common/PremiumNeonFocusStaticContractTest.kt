package com.alaa.iptv.ui.common

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumNeonFocusStaticContractTest {
    @Test
    fun `focus renderer is static and does not create animation objects`() {
        val source = listOf(
            File("src/main/java/com/alaa/iptv/ui/common/PremiumNeonFocus.kt"),
            File("app/src/main/java/com/alaa/iptv/ui/common/PremiumNeonFocus.kt")
        ).firstOrNull { it.isFile }
            ?: error("PremiumNeonFocus.kt is required")

        val code = source.readText()
        assertTrue(code.contains("StaticFocusFrameDrawable"))
        assertFalse(code.contains("ValueAnimator"))
        assertFalse(code.contains("target.animate()"))
        assertFalse(code.contains("FOCUSED_SCALE"))
    }
}
