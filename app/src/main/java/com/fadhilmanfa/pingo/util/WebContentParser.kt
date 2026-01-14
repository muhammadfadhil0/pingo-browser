package com.fadhilmanfa.pingo.util

import com.fadhilmanfa.pingo.data.LinkInfo
import com.fadhilmanfa.pingo.data.PageContent
import org.json.JSONObject

/**
 * Utility object for extracting and parsing web page content
 */
object WebContentParser {
    
    /**
     * JavaScript injection script to extract clean content from web pages.
     * Returns a JSON string with title, description, content, links, and headings.
     */
    val extractionScript = """
        (function() {
            try {
                // Get title
                const title = document.title || '';
                
                // Get meta description
                const metaDesc = document.querySelector('meta[name="description"]');
                const description = metaDesc ? metaDesc.getAttribute('content') || '' : '';
                
                // Get URL
                const url = window.location.href;
                
                // Function to clean text
                function cleanText(text) {
                    return text
                        .replace(/\s+/g, ' ')
                        .replace(/\n+/g, '\n')
                        .trim();
                }
                
                // Remove unwanted elements for content extraction
                function getMainContent() {
                    // Clone body to avoid modifying actual page
                    const clone = document.body.cloneNode(true);
                    
                    // Remove unwanted elements
                    const unwanted = clone.querySelectorAll(
                        'script, style, nav, footer, header, aside, ' +
                        '.advertisement, .ads, .ad, .sidebar, .menu, ' +
                        '.navigation, .nav, .footer, .header, .cookie, ' +
                        '[role="navigation"], [role="banner"], [role="contentinfo"], ' +
                        'iframe, noscript, svg, form, button, input'
                    );
                    unwanted.forEach(el => el.remove());
                    
                    // Try to find main content container
                    const mainContent = clone.querySelector('main, article, [role="main"], .content, .post, .article, #content');
                    const contentSource = mainContent || clone;
                    
                    // Get text content
                    let text = contentSource.innerText || contentSource.textContent || '';
                    text = cleanText(text);
                    
                    // Limit to ~5000 characters
                    if (text.length > 5000) {
                        text = text.substring(0, 5000) + '...';
                    }
                    
                    return text;
                }
                
                // Get headings (H1, H2, H3)
                function getHeadings() {
                    const headings = [];
                    const headingElements = document.querySelectorAll('h1, h2, h3');
                    headingElements.forEach(h => {
                        const text = cleanText(h.innerText || h.textContent || '');
                        if (text && text.length > 0 && text.length < 200) {
                            const level = h.tagName.toLowerCase();
                            headings.push(level + ': ' + text);
                        }
                    });
                    return headings.slice(0, 15); // Limit to 15 headings
                }
                
                // Get important links
                function getLinks() {
                    const links = [];
                    const seen = new Set();
                    const linkElements = document.querySelectorAll('a[href]');
                    
                    linkElements.forEach(a => {
                        const href = a.href;
                        const text = cleanText(a.innerText || a.textContent || '');
                        
                        // Filter valid links
                        if (href && 
                            text && 
                            text.length > 2 && 
                            text.length < 100 &&
                            href.startsWith('http') && 
                            !seen.has(href) &&
                            !href.includes('javascript:')) {
                            seen.add(href);
                            links.push({ text: text, href: href });
                        }
                    });
                    
                    return links.slice(0, 10); // Top 10 links
                }
                
                const result = {
                    url: url,
                    title: title,
                    description: description,
                    mainContent: getMainContent(),
                    headings: getHeadings(),
                    links: getLinks()
                };
                
                // Send to Android
                if (window.Android && window.Android.onContentExtracted) {
                    window.Android.onContentExtracted(JSON.stringify(result));
                }
                
                return JSON.stringify(result);
            } catch (e) {
                const error = { error: e.message };
                if (window.Android && window.Android.onContentExtracted) {
                    window.Android.onContentExtracted(JSON.stringify(error));
                }
                return JSON.stringify(error);
            }
        })();
    """.trimIndent()
    
    /**
     * Parse JSON string from JavaScript extraction to PageContent object
     */
    fun parseFromJson(json: String): PageContent? {
        return try {
            val obj = JSONObject(json)
            
            if (obj.has("error")) {
                return null
            }
            
            val links = mutableListOf<LinkInfo>()
            val linksArray = obj.optJSONArray("links")
            if (linksArray != null) {
                for (i in 0 until linksArray.length()) {
                    val linkObj = linksArray.getJSONObject(i)
                    links.add(LinkInfo(
                        text = linkObj.optString("text", ""),
                        href = linkObj.optString("href", "")
                    ))
                }
            }
            
            val headings = mutableListOf<String>()
            val headingsArray = obj.optJSONArray("headings")
            if (headingsArray != null) {
                for (i in 0 until headingsArray.length()) {
                    headings.add(headingsArray.getString(i))
                }
            }
            
            PageContent(
                url = obj.optString("url", ""),
                title = obj.optString("title", ""),
                description = obj.optString("description", ""),
                mainContent = obj.optString("mainContent", ""),
                links = links,
                headings = headings
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert PageContent to Markdown format for AI consumption
     */
    fun toMarkdown(content: PageContent): String {
        return buildString {
            // Title
            appendLine("# ${content.title}")
            appendLine()
            
            // URL
            appendLine("**URL:** ${content.url}")
            appendLine()
            
            // Description
            if (content.description.isNotBlank()) {
                appendLine("**Deskripsi:** ${content.description}")
                appendLine()
            }
            
            // Headings structure
            if (content.headings.isNotEmpty()) {
                appendLine("## Struktur Halaman")
                content.headings.forEach { heading ->
                    val parts = heading.split(": ", limit = 2)
                    if (parts.size == 2) {
                        val level = parts[0]
                        val text = parts[1]
                        val indent = when (level) {
                            "h1" -> ""
                            "h2" -> "  "
                            "h3" -> "    "
                            else -> ""
                        }
                        appendLine("$indent- $text")
                    }
                }
                appendLine()
            }
            
            // Main content
            appendLine("## Konten Utama")
            appendLine()
            appendLine(content.mainContent)
            appendLine()
            
            // Important links
            if (content.links.isNotEmpty()) {
                appendLine("## Link Penting")
                content.links.forEach { link ->
                    appendLine("- [${link.text}](${link.href})")
                }
            }
        }
    }
}
