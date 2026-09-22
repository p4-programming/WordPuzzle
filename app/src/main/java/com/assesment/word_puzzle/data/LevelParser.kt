package com.assesment.word_puzzle.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object LevelParser {
    fun parse(raw: String): List<Level> {
        val root = Json.parseToJsonElement(raw).jsonObject
        return root.getValue("levels").jsonArray.map { element ->
            val level = element.jsonObject
            Level(
                id = level.getValue("id").jsonPrimitive.int,
                name = level.getValue("name").jsonPrimitive.content.trim(),
                letters = level.getValue("letters").jsonPrimitive.content.trim().uppercase(),
                bonusWords = level.getValue("bonusWords").jsonArray.map { bonus ->
                    bonus.jsonPrimitive.content.trim().uppercase()
                },
                placements = level.getValue("placements").jsonArray.map { placed ->
                    val placement = placed.jsonObject
                    Placement(
                        word = placement.getValue("word").jsonPrimitive.content.trim().uppercase(),
                        row = placement.getValue("row").jsonPrimitive.int,
                        col = placement.getValue("col").jsonPrimitive.int,
                        vertical = placement.getValue("vertical").jsonPrimitive.boolean,
                    )
                },
            )
        }
    }
}
