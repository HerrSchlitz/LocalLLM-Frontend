package com.meshnet.llm;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.autolink.AutolinkExtension;

import java.io.IOException;

/**
 * Renders markdown to HTML for chat messages
 * Supports: bold, italic, code blocks, inline code, lists, headers, links, code blocks
 */
public class MarkdownRenderer {
    
    private static final Parser PARSER = Parser.builder()
            .extension(AutolinkExtension.create())
            .build();
    
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder()
            .build();
    
    /**
     * Converts markdown text to HTML
     */
    public static String renderMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }
        
        Node document = PARSER.parse(markdown);
        return HTML_RENDERER.render(document);
    }
    
    /**
     * Simple markdown-to-HTML converter without external library
     * Use this if you want zero dependencies
     */
    public static String simpleRender(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }
        
        // Escape HTML first
        String html = markdown.replaceAll("&", "&amp;")
                             .replaceAll("<", "&lt;")
                             .replaceAll(">", "&gt;")
                             .replaceAll("'", "&#39;")
                             .replaceAll("\"", "&quot;");
        
        // Headers
        html = html.replaceAll("^######\\s*(.+)$", "<h6>$1</h6>");
        html = html.replaceAll("^#####\\s*(.+)$", "<h5>$1</h5>");
        html = html.replaceAll("^####\\s*(.+)$", "<h4>$1</h4>");
        html = html.replaceAll("^###\\s*(.+)$", "<h3>$1</h3>");
        html = html.replaceAll("^##\\s*(.+)$", "<h2>$1</h2>");
        html = html.replaceAll("^#\\s*(.+)$", "<h1>$1</h1>");
        
        // Bold and italic
        html = html.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
        html = html.replaceAll("\\*([^*]+)\\*", "<em>$1</em>");
        html = html.replaceAll("__([^_]+)__", "<strong>$1</strong>");
        html = html.replaceAll("_([^_]+)_", "<em>$1</em>");
        
        // Inline code
        html = html.replaceAll("`([^`]+)`", "<code class='inline'>$1</code>");
        
        // Code blocks
        html = html.replaceAll("```([\\s\\S]*?)```", "<pre><code class='block'>$1</code></pre>");
        
        // Links
        html = html.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "<a href='$2'>$1</a>");
        
        // Line breaks
        html = html.replaceAll("\n", "<br>");
        
        return html;
    }
    
    /**
     * Apply markdown rendering to an EditText with live update
     */
    public static void applyToEditText(EditText editText, boolean useLibrary) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable editable) {
                if (useLibrary) {
                    try {
                        editText.setText(renderMarkdown(editable.toString()));
                        editText.setMovementMethod(new HtmlMovementMethod());
                    } catch (Exception e) {
                        // Fallback to plain text on error
                        editText.setMovementMethod(null);
                    }
                }
            }
        });
    }
}
