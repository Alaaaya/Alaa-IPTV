package com.alaa.iptv.data.preferences

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class InAppUpdatesFeatureTest {
    @Test
    fun `in app updates stay disabled until explicitly enabled`() {
        val option = FeatureCatalog.options.firstOrNull { it.id == FeatureCatalog.IN_APP_UPDATES }

        assertNotNull(option)
        assertFalse(option!!.defaultEnabled)
    }
}
