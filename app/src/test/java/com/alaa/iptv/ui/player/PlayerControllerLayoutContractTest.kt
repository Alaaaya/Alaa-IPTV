package com.alaa.iptv.ui.player

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerControllerLayoutContractTest {
    @Test
    fun `custom Media3 controller declares exo progress as DefaultTimeBar`() {
        val layoutFile = listOf(
            File("src/main/res/layout/custom_player_controls.xml"),
            File("app/src/main/res/layout/custom_player_controls.xml")
        ).firstOrNull { it.isFile }
            ?: error("custom_player_controls.xml is required for PlayerView")

        val layout = layoutFile.readText()
        assertTrue(layout.contains("androidx.media3.ui.DefaultTimeBar"))
        assertTrue(layout.contains("android:id=\"@+id/exo_progress\""))
        assertFalse(layout.contains("<SeekBar"))
    }
}
