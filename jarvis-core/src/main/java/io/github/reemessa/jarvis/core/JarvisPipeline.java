package io.github.reemessa.jarvis.core;

import static java.util.Objects.requireNonNull;

import io.github.reemessa.jarvis.core.port.AudioRecorder;
import io.github.reemessa.jarvis.core.port.Brain;
import io.github.reemessa.jarvis.core.port.SpeechToText;
import io.github.reemessa.jarvis.core.port.TextToSpeech;
import io.github.reemessa.jarvis.core.port.WakeWordDetector;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * Runs Jarvis's main loop: wait for the wake word, record, transcribe, ask the brain, speak, repeat.
 */
public final class JarvisPipeline {

    private static final Logger LOG = System.getLogger(JarvisPipeline.class.getName());

    private final WakeWordDetector wakeWordDetector;
    private final AudioRecorder audioRecorder;
    private final SpeechToText speechToText;
    private final Brain brain;
    private final TextToSpeech textToSpeech;

    public JarvisPipeline(
            WakeWordDetector wakeWordDetector,
            AudioRecorder audioRecorder,
            SpeechToText speechToText,
            Brain brain,
            TextToSpeech textToSpeech) {
        this.wakeWordDetector = requireNonNull(wakeWordDetector, "wakeWordDetector");
        this.audioRecorder = requireNonNull(audioRecorder, "audioRecorder");
        this.speechToText = requireNonNull(speechToText, "speechToText");
        this.brain = requireNonNull(brain, "brain");
        this.textToSpeech = requireNonNull(textToSpeech, "textToSpeech");
    }

    /**
     * Runs the loop until the thread is interrupted. A stage that fails is logged, and Jarvis goes
     * back to listening for the wake word.
     *
     * @throws InterruptedException when the thread is interrupted, which is how the loop stops
     */
    public void run() throws InterruptedException {
        while (true) {
            try {
                takeTurn();
            } catch (RuntimeException e) {
                LOG.log(Level.ERROR, "Turn failed; listening for the wake word again", e);
            }
        }
    }

    private void takeTurn() throws InterruptedException {
        wakeWordDetector.awaitWakeWord();
        byte[] audio = audioRecorder.recordUntilSilence();
        String userText = speechToText.transcribe(audio);
        String reply = brain.respond(userText);
        textToSpeech.speak(reply);
    }
}
