package com.alaa.iptv.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class SeniorFriendlyListPolicyTest {
    @Test
    fun `enforces large readable rows numbers and names`() {
        assertEquals(58, SeniorFriendlyListPolicy.categoryRowHeightDp(48))
        assertEquals(16f, SeniorFriendlyListPolicy.categoryNameSizeSp(14f))
        assertEquals(58, SeniorFriendlyListPolicy.channelRowHeightDp(52))
        assertEquals(17f, SeniorFriendlyListPolicy.rowNumberSizeSp(12f))
    }
}
