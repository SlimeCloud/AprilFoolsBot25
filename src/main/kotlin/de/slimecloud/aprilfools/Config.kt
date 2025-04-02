package de.slimecloud.aprilfools

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import java.io.File

private const val KEYWORD = "keyword:"

@Serializable
data class Config(
    val sentence: String,
    val revealMessage: String,
    val hints: List<Hint>,
    val minResponseDelay: Int,
    val maxResponseDelay: Int,
    val role: Long
) {
    @Serializable
    data class Hint(
        val trigger: String,
        val hint: String
    ) {
        fun check(content: String): Boolean {
            if (trigger.startsWith(KEYWORD)) {
                val keywords = trigger.substring(KEYWORD.length).lowercase().split(",")
                return content.lowercase().words().any { a -> keywords.any { b -> a.matches("\\W*${Regex.escape(b)}\\W*".toRegex()) } }
            } else return content.matches(trigger.toRegex())
        }
    }

    companion object {
        fun readFromFile(path: String) = Yaml.default.decodeFromString(serializer(), File(path).readText())
    }
}