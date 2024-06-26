package org.pipeman.books.ai.impl;

import org.pipeman.books.ai.Groq;
import org.pipeman.books.ai.TextProcessor;

import java.io.InputStream;

public class GroqTextProcessor extends TextProcessor {
    public GroqTextProcessor(String token) {
        super(token);
    }

    @Override
    public String summarize(String text) {
        return Groq.generateResponse("Zusammenfassen in Stichpunkten in max. 150 Wörtern. Antwort in der Sprache des Textes.", text, token).trim();
    }

    @Override
    public String prepareForTTS(String text) {
        return Groq.generateResponse("""
                Your response will be the input for a TTS system. Your task is to prepare/structure the given text to be read aloud. Keep the wording as much as possible. The given text was generated by a bad OCR, so mistakes are possible. Please correct spelling mistakes.
                Respond in the language of the given text. Your answer should not include anything like "Here is the revised text with spelling and grammar corrections"
                """, text, token);
    }

    @Override
    public InputStream doTTS(String text) {
        throw new UnsupportedOperationException();
    }
}
