package com.simeis512.arkatazuke.gemini

import org.json.JSONObject

/** 写真1枚から Gemini が見つけた「片付け対象」の一覧。 */
data class ClutterAnalysis(
    val summary: String,
    val items: List<ClutterItem>,
)

data class ClutterItem(
    val name: String,
    val category: String,
    val whereInImage: String,
    val suggestedAction: String,
    /** 1 = すぐ片付けるべき, 3 = 後回しでよい */
    val priority: Int,
)

internal object ClutterSchema {
    /** Gemini の構造化出力 (responseSchema) に渡すスキーマ。 */
    val responseSchema: JSONObject = JSONObject(
        """
        {
          "type": "OBJECT",
          "properties": {
            "summary": { "type": "STRING", "description": "部屋の散らかり具合の短い要約（日本語）" },
            "items": {
              "type": "ARRAY",
              "items": {
                "type": "OBJECT",
                "properties": {
                  "name": { "type": "STRING", "description": "物の名前" },
                  "category": { "type": "STRING", "description": "衣類、書類、食器、ゴミ、本、小物 など" },
                  "where_in_image": { "type": "STRING", "description": "画像内の位置（例: 左下の床の上）" },
                  "suggested_action": { "type": "STRING", "description": "どう片付けるか（例: クローゼットに戻す）" },
                  "priority": { "type": "INTEGER", "description": "1=すぐ, 2=普通, 3=後回し" }
                },
                "required": ["name", "category", "where_in_image", "suggested_action", "priority"]
              }
            }
          },
          "required": ["summary", "items"]
        }
        """.trimIndent()
    )

    fun parse(json: String): ClutterAnalysis {
        val root = JSONObject(json)
        val arr = root.optJSONArray("items")
        val items = buildList {
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        ClutterItem(
                            name = o.optString("name"),
                            category = o.optString("category"),
                            whereInImage = o.optString("where_in_image"),
                            suggestedAction = o.optString("suggested_action"),
                            priority = o.optInt("priority", 2),
                        )
                    )
                }
            }
        }
        return ClutterAnalysis(
            summary = root.optString("summary"),
            items = items.sortedBy { it.priority },
        )
    }
}
