package com.alaa.iptv.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveCategoryLayoutPolicyTest {
    @Test
    fun `large theme requests are capped to a compact TV category row`() {
        assertEquals(54, LiveCategoryLayoutPolicy.compactItemHeightDp(78))
        assertEquals(16f, LiveCategoryLayoutPolicy.compactNameSizeSp(18f))
    }

    @Test
    fun `small theme requests retain a readable minimum`() {
        assertEquals(50, LiveCategoryLayoutPolicy.compactItemHeightDp(42))
        assertEquals(14f, LiveCategoryLayoutPolicy.compactNameSizeSp(11f))
    }
}
