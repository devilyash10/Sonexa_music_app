package com.example.sonexa.core.util

// 1. The Data Model
data class LyricLine(
    val timeMs: Long,
    val text: String
)

// 2. The Parser
object LrcParser {
    fun parse(lrcContent: String): List<LyricLine> {
        val timeRegex = "\\[(\\d{2,3}):(\\d{2})\\.(\\d{2,3})\\]".toRegex()
        val parsedList = mutableListOf<LyricLine>()

        lrcContent.lines().forEach { line ->
            val match = timeRegex.find(line)
            if (match != null) {
                // Extract the time variables
                val (min, sec, millisStr) = match.destructured

                // Normalize milliseconds (some formats use 2 digits, some use 3)
                val millis = if (millisStr.length == 2) millisStr.toLong() * 10 else millisStr.toLong()

                // Convert everything to pure milliseconds
                val totalTimeMs = (min.toLong() * 60 * 1000) + (sec.toLong() * 1000) + millis
                val text = line.substring(match.range.last + 1).trim()

                // Only add it if the text isn't empty!
                if (text.isNotEmpty()) {
                    parsedList.add(LyricLine(totalTimeMs, text))
                }
            }
        }
        // Ensure they are always perfectly chronological
        return parsedList.sortedBy { it.timeMs }
    }
}