package com.example.ripple.data.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri

object DeepLinkResolver {
    fun extractInviteToken(intent: Intent?): String? {
        val uri: Uri = intent?.data ?: return null
        
        // Match: https://ripple.app/i/{token}
        if (uri.scheme == "https" && uri.host == "ripple.app" && uri.pathSegments.isNotEmpty()) {
            if (uri.pathSegments[0] == "i" && uri.pathSegments.size >= 2) {
                return uri.pathSegments[1]
            }
            if (uri.pathSegments[0] == "c" && uri.pathSegments.size >= 2) {
                // legacy fallback
                return uri.getQueryParameter("invite") ?: uri.pathSegments[1]
            }
        }

        // Match: ripple://invite/{token}
        if (uri.scheme == "ripple" && uri.host == "invite" && uri.pathSegments.isNotEmpty()) {
            return uri.pathSegments[0]
        }

        return null
    }
}

class InstallReferrerHelper(private val context: Context) {
    private val prefs = context.getSharedPreferences("ripple_deferred_links", Context.MODE_PRIVATE)

    fun saveDeferredInviteToken(token: String) {
        prefs.edit().putString("deferred_invite_token", token).apply()
    }

    fun getAndClearDeferredInviteToken(): String? {
        val token = prefs.getString("deferred_invite_token", null)
        if (token != null) {
            prefs.edit().remove("deferred_invite_token").apply()
        }
        return token
    }
}
