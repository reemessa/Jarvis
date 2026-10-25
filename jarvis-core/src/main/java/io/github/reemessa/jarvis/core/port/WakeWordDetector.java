package io.github.reemessa.jarvis.core.port;

/**
 * Listens for the wake word ("Jarvis").
 */
public interface WakeWordDetector {

    /**
     * Blocks until the wake word is heard.
     *
     * @throws InterruptedException if the thread is interrupted while listening
     */
    void awaitWakeWord() throws InterruptedException;
}
