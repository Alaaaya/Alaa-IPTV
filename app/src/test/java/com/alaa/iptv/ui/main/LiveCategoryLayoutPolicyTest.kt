package com.alaa.iptv.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveCategoryLayoutPolicyTest {
    @Test
    fun `large theme requests are capped to a balanced TV category row`() {
        assertEquals(64, LiveCategoryLayoutPolicy.compactItemHeightDp(78))
        assertEquals(18f, LiveCategoryLayoutPolicy.compactNameSizeSp(18f))
    }

    @Test
    fun `small theme requests retain a readable minimum`() {
        assertEquals(58, LiveCategoryLayoutPolicy.compactItemHeightDp(42))
        assertEquals(16f, LiveCategoryLayoutPolicy.compactNameSizeSp(11f))
    }
}
