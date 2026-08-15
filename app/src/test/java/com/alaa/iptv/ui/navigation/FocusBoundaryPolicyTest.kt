package com.alaa.iptv.ui.navigation

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusBoundaryPolicyTest {
    @Test
    fun `vertical grid keeps focus inside first and last rows`() {
        assertTrue(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_UP,
                position = 3,
                itemCount = 13,
                spanCount = 4,
                orientation = RecyclerView.VERTICAL
            )
        )
        assertFalse(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_UP,
                position = 4,
                itemCount = 13,
                spanCount = 4,
                orientation = RecyclerView.VERTICAL
            )
        )
        assertTrue(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_DOWN,
                position = 12,
                itemCount = 13,
                spanCount = 4,
                orientation = RecyclerView.VERTICAL
            )
        )
    }

    @Test
    fun `horizontal grid keeps focus inside top and bottom of each column`() {
        assertTrue(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_UP,
                position = 4,
                itemCount = 8,
                spanCount = 2,
                orientation = RecyclerView.HORIZONTAL
            )
        )
        assertFalse(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_DOWN,
                position = 6,
                itemCount = 8,
                spanCount = 2,
                orientation = RecyclerView.HORIZONTAL
            )
        )
        assertTrue(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_DOWN,
                position = 7,
                itemCount = 8,
                spanCount = 2,
                orientation = RecyclerView.HORIZONTAL
            )
        )
    }

    @Test
    fun `horizontal arrows remain available for intentional column navigation`() {
        assertFalse(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_LEFT,
                position = 0,
                itemCount = 8,
                spanCount = 2,
                orientation = RecyclerView.VERTICAL
            )
        )
        assertFalse(
            FocusBoundaryPolicy.blocksVerticalExit(
                KeyEvent.KEYCODE_DPAD_RIGHT,
                position = 7,
                itemCount = 8,
                spanCount = 2,
                orientation = RecyclerView.VERTICAL
            )
        )
    }
}
