# AR片付け (ar-katazuke)

XREAL AURA（Android XR）と Gemini を使って、部屋の片付けを支援する AR アプリです。

1. 部屋を見渡してマッピングする
2. Gemini がマップを元に片付けプランを作る
3. AR ラベルで「どこを・どう片付けるか」をガイドする

## 現在の状態（ステップ1）
- Jetpack XR のアプリ雛形
- Gemini API キー（BYOK）とモデル名の設定
- 写真を選ぶ／撮影して Gemini に送り、片付け対象を一覧表示
- 実機確認用に、端末のカメラ一覧を表示

計画と調査メモは [docs/plan.md](docs/plan.md) と [docs/research.md](docs/research.md) にあります。

## ビルドと実行
- Android Studio（最新版）でこのフォルダを開き、Android XR エミュレータか実機で実行します。
- コマンドラインなら `./gradlew assembleDebug`。
- GitHub Actions がビルドしたデバッグ APK は、Actions の実行結果の「Artifacts」からダウンロードできます。

## Gemini APIキー
[Google AI Studio](https://aistudio.google.com/apikey) でキーを作成し、アプリの設定欄に入力してください。
キーは端末内にのみ保存されます。
