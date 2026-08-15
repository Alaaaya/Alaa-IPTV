package com.alaa.iptv.data.preferences

import com.alaa.iptv.data.remote.RemoteConfigValue
import com.alaa.iptv.data.remote.RemoteFeatureFlag
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ControlPlanePolicyTest {
    @Test
    fun `suspended enrolled device is blocked while manual device is not`() {
        assertTrue(ControlPlanePolicy.isDeviceBlocked(enrolled = true, deviceStatus = "suspended"))
        assertTrue(ControlPlanePolicy.isDeviceBlocked(enrolled = true, deviceStatus = "unknown"))
        assertFalse(ControlPlanePolicy.isDeviceBlocked(enrolled = false, deviceStatus = "suspended"))
        assertFalse(ControlPlanePolicy.isDeviceBlocked(enrolled = true, deviceStatus = "active"))
    }

    @Test
    fun `server flags can restrict but never force enable local optional features`() {
        val disabled = mapOf("watchlist" to RemoteFeatureFlag(enabled = false, rolloutPercent = 100))
        val rolloutZero = mapOf("watchlist" to RemoteFeatureFlag(enabled = true, rolloutPercent = 0))
        val enabled = mapOf("watchlist" to RemoteFeatureFlag(enabled = true, rolloutPercent = 100))
        assertFalse(ControlPlanePolicy.isFeatureAllowed("watchlist", "ALA-1234-5678-ABCD", disabled))
        assertFalse(ControlPlanePolicy.isFeatureAllowed("watchlist", "ALA-1234-5678-ABCD", rolloutZero))
        assertTrue(ControlPlanePolicy.isFeatureAllowed("watchlist", "ALA-1234-5678-ABCD", enabled))
        assertTrue(ControlPlanePolicy.isFeatureAllowed("watchlist", "ALA-1234-5678-ABCD", emptyMap()))
    }

    @Test
    fun `remote config hides only its matching home category`() {
        val config = mapOf("home.hideSports" to RemoteConfigValue("true", "boolean"))
        assertTrue(ControlPlanePolicy.isHomeCategoryHidden("sports", config))
        assertFalse(ControlPlanePolicy.isHomeCategoryHidden("movies", config))
        assertFalse(ControlPlanePolicy.isHomeCategoryHidden("movie", config))
    }

    @Test
    fun `maintenance and force update policies are evaluated from remote config`() {
        val maintenance = mapOf(
            "maintenance.enabled" to RemoteConfigValue("true", "boolean"),
            "maintenance.message" to RemoteConfigValue("عودة بعد التحديث", "string")
        )
        val forcedUpdate = mapOf(
            "app.forceUpdate" to RemoteConfigValue("true", "boolean"),
            "app.minimumVersion" to RemoteConfigValue("2.6.0", "string"),
            "app.updateUrl" to RemoteConfigValue("https://example.com/alaa.apk", "string")
        )
        assertTrue(ControlPlanePolicy.isMaintenanceEnabled(maintenance))
        assertTrue(ControlPlanePolicy.maintenanceMessage(maintenance).contains("التحديث"))
        assertTrue(ControlPlanePolicy.isForcedUpdateRequired("2.5.0", forcedUpdate))
        assertFalse(ControlPlanePolicy.isForcedUpdateRequired("2.6.0", forcedUpdate))
        assertTrue(ControlPlanePolicy.updateUrl(forcedUpdate).endsWith("alaa.apk"))
    }
}
