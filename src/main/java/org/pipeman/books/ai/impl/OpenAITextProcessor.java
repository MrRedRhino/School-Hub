package org.pipeman.books.ai.impl;

import org.pipeman.books.ai.OpenAI;
import org.pipeman.books.ai.TextProcessor;

import java.io.InputStream;

public class OpenAITextProcessor extends TextProcessor {
    public OpenAITextProcessor(String token) {
        super(token);
    }

    @Override
    public String summarize(String text) {
        String task = "%s\n\nExtrahiere die wichtigsten Inhalte. Max. 100 Wörter".formatted(text);

        return OpenAI.getCompletion(task, token).trim();
    }

    @Override
    public String prepareForTTS(String text) {
        return "";
    }

    @Override
    public InputStream doTTS(String text) {
        return OpenAI.doTTS(text, token);
    }
}
