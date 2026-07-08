package com.example.mqttclient.util

import org.json.JSONArray
import org.json.JSONObject

object JsonFormatter {
    fun format(text: String): String {
        val trimmed = text.trim()
        return try {
            when {
                trimmed.startsWith("{") -> JSONObject(trimmed).toString(2)
                trimmed.startsWith("[") -> JSONArray(trimmed).toString(2)
                else -> text
            }
        } catch (_: Exception) {
            text
        }
    }

    fun isValidJson(text: String): Boolean {
        val trimmed = text.trim()
        return try {
            when {
                trimmed.startsWith("{") -> { JSONObject(trimmed); true }
                trimmed.startsWith("[") -> { JSONArray(trimmed); true }
                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }
}
