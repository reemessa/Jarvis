package io.github.reemessa.jarvis.core.port;

/**
 * Converts recorded speech to text.
 */
public interface SpeechToText {

    /**
     * Transcribes recorded audio.
     *
     * @param audio 16 kHz, mono, 16-bit little-endian PCM, as produced by {@link AudioRecorder}
     * @return the transcribed text
     */
    String transcribe(byte[] audio);
}
