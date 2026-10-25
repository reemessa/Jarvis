package io.github.reemessa.jarvis.core;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.reemessa.jarvis.core.port.AudioRecorder;
import io.github.reemessa.jarvis.core.port.Brain;
import io.github.reemessa.jarvis.core.port.SpeechToText;
import io.github.reemessa.jarvis.core.port.TextToSpeech;
import io.github.reemessa.jarvis.core.port.WakeWordDetector;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class JarvisPipelineTest {

    private enum Stage { WAKE_WORD, RECORD, TRANSCRIBE, THINK, SPEAK }

    private final Logger pipelineLogger = Logger.getLogger(JarvisPipeline.class.getName());
    private final List<LogRecord> logRecords = new ArrayList<>();
    private final Handler logCapture = new Handler() {
        @Override
        public void publish(LogRecord record) {
            logRecords.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    private final FakeBrain brain = new FakeBrain();
    private final FakeTextToSpeech textToSpeech = new FakeTextToSpeech();
    private final List<Duration> pauses = new ArrayList<>();
    private Stage failingStage;

    @BeforeEach
    void captureLogs() {
        pipelineLogger.addHandler(logCapture);
        pipelineLogger.setUseParentHandlers(false);
    }

    @AfterEach
    void releaseLogs() {
        pipelineLogger.removeHandler(logCapture);
        pipelineLogger.setUseParentHandlers(true);
    }

    @Test
    void speaksAReplyToEachUtteranceUntilInterrupted() {
        var pipeline = pipelineHearing("what time is it", "tell me a joke");

        assertThrows(InterruptedException.class, pipeline::run);

        assertEquals(List.of("You said: what time is it", "You said: tell me a joke"), textToSpeech.spoken);
        assertEquals(List.of(), logRecords);
        assertEquals(List.of(), pauses);
    }

    @ParameterizedTest
    @EnumSource(value = Stage.class, names = {"RECORD", "TRANSCRIBE", "THINK", "SPEAK"})
    void abandonsTheTurnAndListensAgainWhenAStageFails(Stage stage) {
        failingStage = stage;
        var pipeline = pipelineHearing("what time is it", "tell me a joke");

        assertThrows(InterruptedException.class, pipeline::run);

        assertEquals(List.of("You said: tell me a joke"), textToSpeech.spoken);
        assertLoggedFailureOf(stage);
        assertEquals(List.of(), pauses);
    }

    @Test
    void pausesThenListensAgainWhenWakeWordDetectionFails() {
        failingStage = Stage.WAKE_WORD;
        var pipeline = pipelineHearing("what time is it");

        assertThrows(InterruptedException.class, pipeline::run);

        assertEquals(List.of("You said: what time is it"), textToSpeech.spoken);
        assertLoggedFailureOf(Stage.WAKE_WORD);
        assertEquals(List.of(Duration.ofSeconds(1)), pauses);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  \t "})
    void skipsTheBrainWhenNothingWasSaid(String silence) {
        var pipeline = pipelineHearing(silence, "tell me a joke");

        assertThrows(InterruptedException.class, pipeline::run);

        assertEquals(List.of("tell me a joke"), brain.heard);
        assertEquals(List.of("You said: tell me a joke"), textToSpeech.spoken);
        assertTrue(logRecords.stream().noneMatch(r -> r.getLevel() == Level.SEVERE));
    }

    private JarvisPipeline pipelineHearing(String... utterances) {
        return new JarvisPipeline(
                new FakeWakeWordDetector(utterances.length),
                new FakeAudioRecorder(utterances),
                new FakeSpeechToText(),
                brain,
                textToSpeech,
                pauses::add);
    }

    private void assertLoggedFailureOf(Stage stage) {
        assertEquals(1, logRecords.size());
        assertEquals(Level.SEVERE, logRecords.getFirst().getLevel());
        assertEquals(stage + " failed", logRecords.getFirst().getThrown().getMessage());
    }

    private void failOnceAt(Stage stage) {
        if (stage == failingStage) {
            failingStage = null;
            throw new IllegalStateException(stage + " failed");
        }
    }

    /** Hears the wake word a fixed number of times, then interrupts to stop the pipeline. */
    private final class FakeWakeWordDetector implements WakeWordDetector {
        private int wakeWordsLeft;

        FakeWakeWordDetector(int wakeWords) {
            this.wakeWordsLeft = wakeWords;
        }

        @Override
        public void awaitWakeWord() throws InterruptedException {
            failOnceAt(Stage.WAKE_WORD);
            if (wakeWordsLeft-- == 0) {
                throw new InterruptedException("no more wake words");
            }
        }
    }

    /** Records each scripted utterance in turn, encoded as bytes. */
    private final class FakeAudioRecorder implements AudioRecorder {
        private final Iterator<String> utterances;

        FakeAudioRecorder(String... utterances) {
            this.utterances = List.of(utterances).iterator();
        }

        @Override
        public byte[] recordUntilSilence() {
            // The user still spoke, so the utterance is used up even when recording fails.
            String utterance = utterances.next();
            failOnceAt(Stage.RECORD);
            return utterance.getBytes(UTF_8);
        }
    }

    /** Decodes the fake recorder's bytes back into text. */
    private final class FakeSpeechToText implements SpeechToText {
        @Override
        public String transcribe(byte[] audio) {
            failOnceAt(Stage.TRANSCRIBE);
            return new String(audio, UTF_8);
        }
    }

    /** Echoes the user's text back, remembering everything it was asked. */
    private final class FakeBrain implements Brain {
        private final List<String> heard = new ArrayList<>();

        @Override
        public String respond(String userText) {
            heard.add(userText);
            failOnceAt(Stage.THINK);
            return "You said: " + userText;
        }
    }

    /** Remembers everything it was asked to say. */
    private final class FakeTextToSpeech implements TextToSpeech {
        private final List<String> spoken = new ArrayList<>();

        @Override
        public void speak(String text) {
            failOnceAt(Stage.SPEAK);
            spoken.add(text);
        }
    }
}
