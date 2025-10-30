package com.example.dndcharacterviewer.data

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.UUID

data class CharacterSummary(
    val id: String,        // internal id (UUID based on file name)
    val name: String,
    val clazz: String?,
    val level: Int?,
    val file: File
)

class CharacterStore(private val context: Context) {
    private val dir: File by lazy { File(context.filesDir, "characters").apply { mkdirs() } }

    fun list(): List<CharacterSummary> =
        dir.listFiles { f -> f.isFile && f.name.endsWith(".json", ignoreCase = true) }
            ?.mapNotNull { toSummary(it) }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

    fun importFrom(uri: Uri): CharacterSummary {
        val input = context.contentResolver.openInputStream(uri)
            ?: error("Cannot open input stream")
        val file = writeCopy(input)
        return toSummary(file) ?: error("Invalid character JSON")
    }

    private fun writeCopy(input: InputStream): File {
        val target = File(dir, "${UUID.randomUUID()}.json")
        target.outputStream().use { out -> input.copyTo(out) }
        return target
    }

    /**
     * Parse only what we need for the list (Dungeon Masters Vault format).
     * If JSON contains multiple characters, we’ll show the first one for the card title.
     * (You can extend to multi-character rows later.)
     */
    private fun toSummary(file: File): CharacterSummary? = runCatching {
        val text = file.readText()
        val root = JSONObject(text)
        val characters = root.optJSONArray("character") ?: return null
        if (characters.length() == 0) return null
        val c = characters.getJSONObject(0)

        val name = c.optString("character_name", file.nameWithoutExtension)
            .ifBlank { file.nameWithoutExtension }

        // classes → "blood-hunter" (current project uses that)
        val classesObj = c.optJSONObject("classes")
        val bloodHunter = classesObj?.optJSONObject("blood-hunter")
        val className = bloodHunter?.optString("class-name")?.takeIf { it.isNotBlank() }
        val level = bloodHunter?.optInt("class-level").takeIf { it != null && it != 0 }

        CharacterSummary(
            id = file.nameWithoutExtension,
            name = name,
            clazz = className,
            level = level,
            file = file
        )
    }.getOrNull()
}
