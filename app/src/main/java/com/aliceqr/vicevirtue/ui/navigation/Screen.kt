package com.aliceqr.vicevirtue.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddTrackable : Screen("add_trackable?type={type}&trackableId={trackableId}") {
        fun createRoute(type: String? = null, trackableId: Long? = null): String {
            val params = mutableListOf<String>()
            type?.let { params.add("type=$it") }
            trackableId?.let { params.add("trackableId=$it") }
            return if (params.isEmpty()) "add_trackable" else "add_trackable?${params.joinToString("&")}"
        }
    }
    object LogEvent : Screen("log_event/{trackableId}") {
        fun createRoute(id: Long) = "log_event/$id"
    }
    object History : Screen("history?trackableId={trackableId}&type={type}") {
        fun createRoute(trackableId: Long? = null, type: String? = null): String {
            val params = mutableListOf<String>()
            trackableId?.let { params.add("trackableId=$it") }
            type?.let { params.add("type=$it") }
            return if (params.isEmpty()) "history" else "history?${params.joinToString("&")}"
        }
    }
    object Detail : Screen("detail/{trackableId}") {
        fun createRoute(id: Long) = "detail/$id"
    }
    object Settings : Screen("settings")
}
