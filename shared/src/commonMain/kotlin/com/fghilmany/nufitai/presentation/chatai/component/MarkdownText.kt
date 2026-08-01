package com.fghilmany.nufitai.presentation.chatai.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Lightweight **bold** / *italic* markdown parser for chat replies -- the LLM (chat-send) formats
 * answers with markdown-style emphasis, but Compose's plain `Text` doesn't interpret markdown at
 * all, so without this the asterisks render literally. Deliberately NOT a full markdown engine
 * (no headers/links/code blocks/nested lists) -- bullets and numbered lists already render fine
 * as-is since the model emits literal `•`/`1.` characters, not markdown list syntax that needs
 * transforming. No new dependency added; a real markdown-renderer library is a bigger jump than
 * this narrow need justifies.
 */
private val boldOrItalicRegex = Regex("\\*\\*(.+?)\\*\\*|\\*(.+?)\\*")

fun parseSimpleMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var lastIndex = 0
    for (match in boldOrItalicRegex.findAll(text)) {
        append(text.substring(lastIndex, match.range.first))
        val bold = match.groups[1]?.value
        if (bold != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
        } else {
            val italic = match.groups[2]?.value.orEmpty()
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(italic) }
        }
        lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
}
