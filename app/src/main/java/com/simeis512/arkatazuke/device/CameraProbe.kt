package com.simeis512.arkatazuke.device

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

/**
 * 端末のカメラ一覧を調べる。XREAL AURA でアプリから外向きカメラを使えるかを
 * 実機到着後に確認するためのもの（Android XR ヘッドセットでは Camera2/CameraX で
 * 外向きカメラにアクセスできるが、有線XRグラスでの扱いは未確認）。
 */
object CameraProbe {
    fun describe(context: Context): String {
        val manager = context.getSystemService(CameraManager::class.java)
            ?: return "CameraManager を取得できませんでした"
        val ids = runCatching { manager.cameraIdList.toList() }
            .getOrElse { return "カメラ一覧の取得に失敗: ${it.message}" }
        if (ids.isEmpty()) return "カメラが見つかりません（0台）"
        return ids.joinToString("\n") { id ->
            val c = manager.getCameraCharacteristics(id)
            val facing = when (c.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_BACK -> "外向き(BACK)"
                CameraCharacteristics.LENS_FACING_FRONT -> "内向き(FRONT)"
                CameraCharacteristics.LENS_FACING_EXTERNAL -> "外部(EXTERNAL)"
                else -> "不明"
            }
            val map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val maxSize = map?.getOutputSizes(android.graphics.ImageFormat.JPEG)
                ?.maxByOrNull { it.width * it.height }
            "ID $id: $facing, 最大JPEG ${maxSize ?: "不明"}"
        }
    }
}
