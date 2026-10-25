package io.github.reemessa.jarvis.core;

import static java.util.Objects.requireNonNull;

import io.github.reemessa.jarvis.core.port.AudioRecorder;
import io.github.reemessa.jarvis.core.port.Brain;
import io.github.reemessa.jarvis.core.port.SpeechToText;
import io.github.reemessa.jarvis.core.port.TextToSpeech;
import io.github.reemessa.jarvis.core.port.WakeWordDetector;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Duration;

/**
 * Runs Jarvis's main loop: wait for the wake word, record, transcribe, ask the brain, speak, repeat.
 */
public final class JarvisPipeline {

    private static final Logger LOG = System.getLogger(JarvisPipeline.class.getName());
    private static final Duration WAKE_WORD_RETRY_PAUSE = Duration.ofSeconds(1);

    private final WakeWordDetector wakeWordDetector;
    private final AudioRecorder audioRecorder;
    private final SpeechToText speechToText;
    private final Brain brain;
    private final TextToSpeech textToSpeech;
    private final Sleeper sleeper;

    public JarvisPipeline(
            WakeWordDetector wakeWordDetector,
            AudioRecorder audioRecorder,
            SpeechToText speechToText,
            Brain brain,
            TextToSpeech textToSpeech) {
        this(wakeWordDetector, audioRecorder, speechToText, brain, textToSpeech, Thread::sleep);
    }

    JarvisPipeline(
            WakeWordDetector wakeWordDetector,
            AudioRecorder audioRecorder,
            SpeechToText speechToText,
            Brain brain,
            TextToSpeech textToSpeech,
            Sleeper sleeper) {
        this.wakeWordDetector = requireNonNull(wakeWordDetector, "wakeWordDetector");
        this.audioRecorder = requireNonNull(audioRecorder, "audioRecorder");
        this.speechToText = requireNonNull(speechToText, "speechToText");
        this.brain = requireNonNull(brain, "brain");
        this.textToSpeech = requireNonNull(textToSpeech, "textToSpeech");
        this.sleeper = requireNonNull(sleeper, "sleeper");
    }

    /**
     * Runs the loop until the thread is interrupted. A stage that fails is logged, and Jarvis goes
     * back to listening for the wake word. If the wake word detector itself fails, Jarvis pauses
     * briefly first, so a detector that keeps failing doesn't spin the loop.
     *
     * @throws InterruptedException when the thread is interrupted, which is how the loop stops
     */
    public void run() throws InterruptedException {
        while (true) {
            try {
                wakeWordDetector.awaitWakeWord();
            } catch (RuntimeException e) {
                LOG.log(Level.ERROR, "Wake word detection failed; pausing before listening again", e);
                sleeper.sleep(WAKE_WORD_RETRY_PAUSE);
                continue;
            }
            try {
                takeTurn();
            } catch (RuntimeException e) {
                LOG.log(Level.ERROR, "Turn failed; listening for the wake word again", e);
            }
        }
    }

    private void takeTurn() throws InterruptedException {
        byte[] audio = audioRecorder.recordUntilSilence();
        String userText = speechToText.transcribe(audio);
        if (userText.isBlank()) {
            LOG.log(Level.INFO, "Heard nothing; listening for the wake word again");
            return;
        }
        String reply = brain.respond(userText);
        textToSpeech.speak(reply);
    }

    /** Pauses the loop. Tests swap it out so they don't really sleep. */
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }
}
