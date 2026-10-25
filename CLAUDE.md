# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Jarvis is a voice-driven personal AI assistant in Java: wake on "Jarvis", record, transcribe, ask the brain, speak the reply. The design, adapter choices and milestone plan are in `docs/architecture.md`; read it before starting a milestone.

## Commands

Requires JDK 25 (`maven.compiler.release` in the root `pom.xml`). Use the Maven wrapper, which pins Maven 3.9.12.

```sh
./mvnw verify                                   # build and test all modules
./mvnw -pl jarvis-core test                     # test one module
./mvnw -pl jarvis-core test -Dtest=JarvisPipelineTest
./mvnw -pl jarvis-core test -Dtest='JarvisPipelineTest#skipsTheBrainWhenNothingWasSaid'
```

`jarvis-adapters` and `jarvis-app` depend on `jarvis-core`, so add `-am` when building them alone with `-pl`. There is no linter or formatter configured.

## Architecture

Hexagonal (ports and adapters), split across three Maven modules:

- **`jarvis-core`**: the port interfaces (`core.port`) and `JarvisPipeline`, the orchestrator. It must stay free of external dependencies; JUnit is its only (test) dependency.
- **`jarvis-adapters`**: one package per adapter implementing a port (Porcupine wake word, whisper.cpp STT, Claude API brain, Piper TTS). No adapters exist yet.
- **`jarvis-app`**: wiring, configuration and `main`. Not written yet.

Contracts that span files:

- **Stopping and errors.** `JarvisPipeline.run()` loops forever and stops only when a port throws `InterruptedException`, which is why every blocking port method declares it. A `RuntimeException` from any stage is logged at ERROR, the turn is abandoned, and Jarvis goes back to listening. If the wake word detector itself throws, the loop pauses 1s first (through the package-private `Sleeper` seam) so a broken detector doesn't spin. A blank transcript skips the brain and logs at INFO.
- **Audio format.** `AudioRecorder` produces, and `SpeechToText` consumes, 16 kHz mono 16-bit little-endian PCM. Adapters on both sides must agree on this.
- **Logging.** Core logs through `System.Logger` with no logging library. `JarvisPipelineTest` asserts on log output by attaching a `java.util.logging` handler (`System.Logger` ERROR shows up as JUL `SEVERE`). Adding a logging backend that replaces the default `System.LoggerFinder` would break those tests.
- **Configuration.** API keys (Picovoice, Anthropic) come from environment variables and are never committed.

## Testing conventions

Tests use hand-written fakes as inner classes, not a mocking library. `JarvisPipelineTest` scripts a session: `FakeWakeWordDetector` fires once per scripted utterance and then throws `InterruptedException` to end `run()`, and `failOnceAt(Stage)` makes a chosen stage throw once. Seams that only tests need (like `Sleeper`) go on a package-private constructor; the public constructor supplies the real implementation.

## Workflow

- `main` is protected and only takes pull requests (zero approvals required).
- One feature branch per milestone, named like `feature/m1-hears`. Milestones in order: Hears, Thinks, Speaks, Remembers, Acts, Draco (cloned voice).
- Architecture decisions are recorded in `docs/adr/` as `ADR-NNN-<slug>.md`, each with Context, Decision and Trade-offs accepted sections.
