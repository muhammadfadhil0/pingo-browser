package com.fadhilmanfa.pingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.ui.theme.Secondary

/**
 * A Composable that renders Markdown-formatted text with proper styling.
 * Supports: Headers (H1-H3), Bold, Italic, Bullets, Numbered lists, Code blocks, Inline code, and Tables.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black
) {
    val lines = markdown.lines()
    var i = 0
    
    Column(modifier = modifier) {
        while (i < lines.size) {
            val line = lines[i]
            val trimmedLine = line.trim()
            
            when {
                // Code block (```)
                trimmedLine.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    CodeBlock(code = codeLines.joinToString("\n"))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Headers
                trimmedLine.startsWith("### ") -> {
                    HeaderText(
                        text = trimmedLine.removePrefix("### "),
                        level = 3,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                trimmedLine.startsWith("## ") -> {
                    HeaderText(
                        text = trimmedLine.removePrefix("## "),
                        level = 2,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                trimmedLine.startsWith("# ") -> {
                    HeaderText(
                        text = trimmedLine.removePrefix("# "),
                        level = 1,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                
                // Horizontal rule
                trimmedLine == "---" || trimmedLine == "***" || trimmedLine == "___" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Bullet list
                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") || trimmedLine.startsWith("• ") -> {
                    val indent = line.takeWhile { it == ' ' }.length / 2
                    BulletItem(
                        text = trimmedLine.removePrefix("- ").removePrefix("* ").removePrefix("• "),
                        indent = indent,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Numbered list
                trimmedLine.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val number = trimmedLine.takeWhile { it.isDigit() }
                    val text = trimmedLine.removePrefix("$number. ")
                    val indent = line.takeWhile { it == ' ' }.length / 2
                    NumberedItem(
                        number = number,
                        text = text,
                        indent = indent,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Table (starts with |)
                trimmedLine.startsWith("|") && trimmedLine.endsWith("|") -> {
                    val tableLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().let { it.startsWith("|") && it.endsWith("|") }) {
                        tableLines.add(lines[i].trim())
                        i++
                    }
                    i-- // Adjust because we'll increment at the end
                    if (tableLines.isNotEmpty()) {
                        MarkdownTable(tableLines = tableLines, textColor = textColor)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Blockquote
                trimmedLine.startsWith("> ") -> {
                    BlockQuote(
                        text = trimmedLine.removePrefix("> "),
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Empty line
                trimmedLine.isEmpty() -> {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Regular paragraph with inline formatting
                else -> {
                    FormattedText(text = trimmedLine, textColor = textColor)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            i++
        }
    }
}

@Composable
private fun HeaderText(text: String, level: Int, textColor: Color) {
    val (fontSize, fontWeight) = when (level) {
        1 -> 22.sp to FontWeight.Bold
        2 -> 18.sp to FontWeight.SemiBold
        else -> 16.sp to FontWeight.Medium
    }
    
    Text(
        text = parseInlineFormatting(text),
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = textColor,
        lineHeight = fontSize * 1.3
    )
}

@Composable
private fun BulletItem(text: String, indent: Int, textColor: Color) {
    Row(
        modifier = Modifier.padding(start = (indent * 16).dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Secondary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseInlineFormatting(text),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NumberedItem(number: String, text: String, indent: Int, textColor: Color) {
    Row(
        modifier = Modifier.padding(start = (indent * 16).dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Secondary,
            modifier = Modifier.width(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = parseInlineFormatting(text),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF37474F),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun BlockQuote(text: String, textColor: Color) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(Secondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = parseInlineFormatting(text),
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            color = textColor.copy(alpha = 0.8f),
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownTable(tableLines: List<String>, textColor: Color) {
    val rows = tableLines
        .filter { !it.contains("---") && !it.contains(":-") && !it.contains("-:") }
        .map { row ->
            row.trim('|').split("|").map { it.trim() }
        }
    
    if (rows.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
    ) {
        rows.forEachIndexed { index, cells ->
            val isHeader = index == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isHeader) Modifier.background(Color(0xFFEEEEEE))
                        else Modifier
                    )
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                cells.forEach { cell ->
                    Text(
                        text = cell,
                        fontSize = 13.sp,
                        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (index < rows.size - 1) {
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun FormattedText(text: String, textColor: Color) {
    Text(
        text = parseInlineFormatting(text),
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = textColor
    )
}

/**
 * Parse inline formatting: **bold**, *italic*, `code`, ***bold italic***
 */
private fun parseInlineFormatting(text: String) = buildAnnotatedString {
    var currentIndex = 0
    val boldItalicPattern = Regex("\\*\\*\\*(.+?)\\*\\*\\*")
    val boldPattern = Regex("\\*\\*(.+?)\\*\\*")
    val italicPattern = Regex("\\*(.+?)\\*")
    val codePattern = Regex("`(.+?)`")
    
    // Find all matches and sort by position
    data class Match(val start: Int, val end: Int, val text: String, val style: SpanStyle)
    
    val matches = mutableListOf<Match>()
    
    boldItalicPattern.findAll(text).forEach {
        matches.add(Match(it.range.first, it.range.last + 1, it.groupValues[1], 
            SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)))
    }
    boldPattern.findAll(text).forEach {
        if (matches.none { m -> it.range.first >= m.start && it.range.first < m.end }) {
            matches.add(Match(it.range.first, it.range.last + 1, it.groupValues[1], 
                SpanStyle(fontWeight = FontWeight.Bold)))
        }
    }
    italicPattern.findAll(text).forEach {
        if (matches.none { m -> it.range.first >= m.start && it.range.first < m.end }) {
            matches.add(Match(it.range.first, it.range.last + 1, it.groupValues[1], 
                SpanStyle(fontStyle = FontStyle.Italic)))
        }
    }
    codePattern.findAll(text).forEach {
        if (matches.none { m -> it.range.first >= m.start && it.range.first < m.end }) {
            matches.add(Match(it.range.first, it.range.last + 1, it.groupValues[1], 
                SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFEEEEEE))))
        }
    }
    
    matches.sortBy { it.start }
    
    matches.forEach { match ->
        if (currentIndex < match.start) {
            append(text.substring(currentIndex, match.start))
        }
        withStyle(match.style) {
            append(match.text)
        }
        currentIndex = match.end
    }
    
    if (currentIndex < text.length) {
        append(text.substring(currentIndex))
    }
}
