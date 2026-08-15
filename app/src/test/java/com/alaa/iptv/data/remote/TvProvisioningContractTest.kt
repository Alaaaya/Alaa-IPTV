package com.alaa.iptv.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvProvisioningContractTest {
    @Test
    fun `parses the exact safe devices sync fields returned by the control plane`() {
        val snapshot = TvProvisioningClient.buildControlPlaneSnapshot(
            tvId = "ALA-ABCD-EF12-3456",
            deviceStatus = "suspended",
            remoteLogoutRequested = true,
            remoteConfig = mapOf("home.hideSports" to RemoteConfigValue("true", "boolean")),
            featureFlags = mapOf("library" to RemoteFeatureFlag(false, 100))
        )

        assertEquals("ALA-ABCD-EF12-3456", snapshot.tvId)
        assertEquals("suspended", snapshot.deviceStatus)
        assertTrue(snapshot.remoteLogoutRequested)
        assertEquals("true", snapshot.remoteConfig.getValue("home.hideSports").value)
        assertEquals("boolean", snapshot.remoteConfig.getValue("home.hideSports").type)
        assertFalse(snapshot.featureFlags.getValue("library").enabled)
        assertEquals(100, snapshot.featureFlags.getValue("library").rolloutPercent)
    }
}
