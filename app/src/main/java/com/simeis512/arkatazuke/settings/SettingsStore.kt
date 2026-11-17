package com.simeis512.arkatazuke.settings

import android.content.Context

/**
 * BYOK の Gemini API キーとモデル名を端末内に保存する。
 * キーはアプリ外に送らず、Gemini API へのリクエストヘッダーにだけ使う。
 * TODO(公開前): Android Keystore による暗号化を検討する。
 */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var model: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value.trim().ifEmpty { DEFAULT_MODEL }).apply()

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL = "gemini_model"

        /** 常に最新の Flash を指すエイリアス。固定したい場合は設定画面で変更する。 */
        const val DEFAULT_MODEL = "gemini-flash-latest"
    }
}
