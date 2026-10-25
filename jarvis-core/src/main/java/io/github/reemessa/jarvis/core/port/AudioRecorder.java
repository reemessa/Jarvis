package io.github.reemessa.jarvis.core.port;

/**
 * Captures the user's speech from the microphone.
 */
public interface AudioRecorder {

    /**
     * Records until the user stops speaking.
     *
     * @return the recorded audio as 16 kHz, mono, 16-bit little-endian PCM
     * @throws InterruptedException if the thread is interrupted while recording
     */
    byte[] recordUntilSilence() throws InterruptedException;
}
