package com.alaa.iptv.ui.player

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerOverlayLayoutContractTest {
    @Test
    fun `player waiting overlay provides channel state and retry affordance`() {
        val layoutFile = listOf(
            File("src/main/res/layout/activity_player.xml"),
            File("app/src/main/res/layout/activity_player.xml")
        ).firstOrNull { it.isFile }
            ?: error("activity_player.xml is required for player state UI")

        val layout = layoutFile.readText()
        assertTrue(layout.contains("@+id/playerStatusOverlay"))
        assertTrue(layout.contains("@+id/playerStatusChannel"))
        assertTrue(layout.contains("@+id/playerStatusTitle"))
        assertTrue(layout.contains("@+id/playerStatusMessage"))
        assertTrue(layout.contains("@+id/errorText"))
        assertTrue(layout.contains("@+id/loadingProgress"))
    }
}
