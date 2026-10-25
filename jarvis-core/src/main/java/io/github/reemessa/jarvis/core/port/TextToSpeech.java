package io.github.reemessa.jarvis.core.port;

/**
 * Speaks text aloud.
 */
public interface TextToSpeech {

    /**
     * Converts text to audio and plays it, blocking until playback finishes.
     *
     * @param text the text to speak
     * @throws InterruptedException if the thread is interrupted during playback
     */
    void speak(String text) throws InterruptedException;
}
