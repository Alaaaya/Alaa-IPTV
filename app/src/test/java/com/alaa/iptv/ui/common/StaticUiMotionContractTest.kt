package com.alaa.iptv.ui.common

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class StaticUiMotionContractTest {
    @Test
    fun `android UI source does not use animation or smooth scrolling APIs`() {
        val sourceRoot = listOf(
            File("src/main/java"),
            File("app/src/main/java")
        ).firstOrNull { it.isDirectory }
            ?: error("Android source directory is required")

        val forbidden = listOf(
            "ValueAnimator",
            "ObjectAnimator",
            ".animate()",
            "AnimationUtils",
            "startAnimation",
            "smoothScrollToPosition"
        )
        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { source ->
                val code = source.readText()
                forbidden.forEach { token ->
                    assertFalse("${source.path} must not contain $token", code.contains(token))
                }
            }
    }
}
