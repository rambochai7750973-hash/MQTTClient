package com.example.mqttclient.ui.util

import org.json.JSONArray
import org.json.JSONObject

object PayloadFormatter {

    fun format(payload: ByteArray): FormatResult {
        val text = try {
            String(payload, Charsets.UTF_8)
        } catch (_: Exception) {
            return FormatResult(DisplayMode.HEX, payload.joinToString(" ") { "%02x".format(it) })
        }

        if (text.startsWith("{") || text.startsWith("[")) {
            try {
                val formatted = if (text.startsWith("{")) {
                    JSONObject(text).toString(2)
                } else {
                    JSONArray(text).toString(2)
                }
                return FormatResult(DisplayMode.JSON, formatted)
            } catch (_: Exception) { }
        }

        if (text.all { it.isWhitespace() || it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            // Could be hex-like
        }

        return FormatResult(DisplayMode.TEXT, text)
    }

    fun toHex(payload: ByteArray): String {
        return payload.joinToString(" ") { "%02x".format(it) }
    }

    data class FormatResult(
        val displayMode: DisplayMode,
        val text: String
    )

    enum class DisplayMode { TEXT, JSON, HEX }
}
