# 調査メモ（2026-11-18 時点）

## Android XR / Jetpack XR SDK
- ARCore for Jetpack XR は 2026-10-21 に 1.0.0 安定版。最新 alpha は 1.1.0-alpha01。
  - 平面検出（床・壁・テーブルなどのラベル付き）、アンカー（永続化も可）、ヒットテスト、ハンドトラッキング、
    デプス、QR コード / AugmentedImage、Geospatial など。
  - 1.1.0-alpha01 で `SpatialAnnotation`（カメラ画像から四角形を追跡）が追加。
  - 出典: https://developer.android.com/jetpack/androidx/releases/xr-arcore
- Jetpack Compose for XR / SceneCore / ARCore は「ヘッドセットと有線XRグラス」が対象。AURA は有線XRグラス。
- 公式サンプル android/xr-samples（2026-10-22 更新）のバージョン構成をそのまま採用:
  AGP 9.4.1, Kotlin 2.4.21, compileSdk 37, Compose for XR 1.0.0-beta02, SceneCore 1.1.0-alpha01。
- Developer Preview 4（2026-05）で XR エミュレータの新しいセットアップ手順あり。
  - 出典: https://developer.android.com/blog/posts/updates-to-the-android-xr-sdk-introducing-developer-preview-4

## XREAL AURA
- Snapdragon 搭載の有線パック + 光学シースルー（視野角約70度）。空間トラッキングとハンドトラッキングに対応。
- 出典: https://virtual.reality.news/news/xreal-aura-enterprise-deployment-gets-early-evaluation-path/

## 最大のリスク: アプリから AURA のカメラ映像を取れるか
- Android XR ヘッドセットでは、CAMERA 権限で Camera2 / CameraX から外向きカメラを使えると Google が説明済み。
- 有線XRグラス（AURA）で同じことができるかは、公式情報では確認できなかった。
  2026-11-03 時点で Unreal フォーラムにも同じ質問があり、回答なし。
  - https://forums.unrealengine.com/t/accessing-the-rgb-camera-feed-from-unreal-on-android-xr-wired-glasses-xreal-aura/2849586
- **実機到着後の最初の確認事項**: アプリの「カメラ一覧を調べる」で外向きカメラが見えるか。
- 取れなかった場合の代替案:
  1. スマホで部屋の写真を撮り、AURA 側アプリへ送る（または同じアプリのスマホ版で撮る）
  2. ARCore の平面・デプス情報だけでマップを作り、Gemini には言葉（音声入力）で状況を伝える
  3. スクリーンキャプチャ（MediaProjection）でパススルー込みの画面を取れるか試す（光学シースルーなので映らない可能性が高い）

## Gemini API
- REST `v1beta/models/{model}:generateContent`、ヘッダー `x-goog-api-key`。
- 構造化出力（`responseMimeType: application/json` + `responseSchema`）で一覧を受け取る。
- 既定モデルは `gemini-flash-latest`（エイリアス）。アプリの設定で変更可能。
- 今後の候補: リアルタイム対話には Gemini Live API（音声・映像ストリーミング）。
