package org.pipeman.books.ai.impl;

import org.pipeman.books.ai.Summarizer;

public class GroqSummarizer extends Summarizer {
    public GroqSummarizer(String token) {
        super(token);
    }

    @Override
    public String summarize(String text) {
        return Groq.getSummary(text, token).trim();
    }
}
