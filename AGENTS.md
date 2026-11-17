# AGENTS.md

AI コーディングエージェント（Claude Code / Codex など）向けのメモです。

## プロジェクト概要
XREAL AURA（Android XR の有線XRグラス）で部屋を見渡してマッピングし、
Gemini が片付けプランを作り、AR で片付けをガイドするアプリ。
目標: LODGE XR Talk（2027年1月ごろ）での技術デモ。

## 技術スタック
- Kotlin + Jetpack Compose + Jetpack XR SDK（Compose for XR / SceneCore / ARCore for Jetpack XR）
- Gradle バージョンカタログ: `gradle/libs.versions.toml`（android/xr-samples に合わせている）
- Gemini: REST の `generateContent` を `HttpURLConnection` で直接呼ぶ（BYOK、外部 SDK なし）
  - 結果は `responseSchema` で構造化 JSON にする
- 追加ライブラリは必要になるまで増やさない

## ビルド
- `./gradlew assembleDebug`（JDK 17、Android SDK が必要）
- CI: `.github/workflows/build.yml` が push ごとにビルドし、デバッグ APK を成果物に上げる

## コード構成（app/src/main/java/com/simeis512/arkatazuke）
- `MainActivity.kt` エントリポイント
- `ui/` 画面（XR では SpatialPanel、通常端末では全画面）
- `gemini/` Gemini クライアントと応答モデル
- `settings/` APIキー・モデル名の保存
- `device/` 実機能力の確認用ユーティリティ

## 約束ごと
- UI 文言・コメントは日本語
- API キーをログやリポジトリに出さない
- XR 固有 API は `LocalSpatialCapabilities` などで分岐し、非XR端末でも起動できるようにする
- 調査結果や未確認事項は `docs/` に残す
