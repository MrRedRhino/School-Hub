package org.pipeman.books.ai;

import java.io.InputStream;

public abstract class TextProcessor {
    protected final String token;

    public TextProcessor(String token) {
        this.token = token;
    }

    public abstract String summarize(String text);

    public abstract String prepareForTTS(String text);

    public abstract InputStream doTTS(String text);
}
