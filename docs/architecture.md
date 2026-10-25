# Jarvis — Architecture v1

## Purpose

A personal, voice-driven AI assistant for Reem. Version one: wake on "Jarvis", listen, think, and answer by voice.

## Architectural Style

Hexagonal architecture (ports and adapters). The core pipeline depends only on interfaces (ports). Each external technology is an adapter that can be swapped without touching the core.

## Pipeline

```
Wake word → Record audio → Speech-to-text → Brain → Text-to-speech → Play audio
```

## Ports (Java interfaces)

- **`WakeWordDetector`** — blocks until the wake word is heard.
- **`AudioRecorder`** — captures speech until silence.
- **`SpeechToText`** — converts recorded audio to text.
- **`Brain`** — takes the user's text (plus conversation context) and returns a reply.
- **`TextToSpeech`** — converts reply text to audio and plays it.

## Adapters (v1)

- `WakeWordDetector` → Picovoice Porcupine (Java SDK, built-in "Jarvis" keyword, local)
- `SpeechToText` → whisper.cpp (local, via JNI binding or local process)
- `Brain` → Claude API (billed separately from the Claude subscription)
- `TextToSpeech` → Piper (local)
- Future: voice-cloning adapter (e.g. XTTS) for the Draco Malfoy voice

## Module Layout (proposal)

- **`jarvis-core`** — ports, pipeline orchestrator, domain model. No external dependencies.
- **`jarvis-adapters`** — one package per adapter.
- **`jarvis-app`** — wiring/configuration and the main entry point.

## Cross-cutting Concerns

- **Configuration:** API keys (Picovoice, Anthropic) from environment variables, never committed.
- **Logging:** each pipeline stage logs timing, to spot latency bottlenecks.
- **Errors:** a failing stage returns Jarvis to listening for the wake word.

## Architecture Decision Records

Stored in the repo under [`docs/adr/`](adr/).

- ADR-001: Java for v1
- ADR-002: Local speech (whisper.cpp for STT, Piper for TTS)
- ADR-003: Claude API as the brain
- ADR-004: Wake word from v1 using Porcupine
- ADR-005: Hexagonal architecture

## Milestones

1. **Hears** — wake word triggers recording; whisper.cpp transcribes; text printed to console.
2. **Thinks** — transcribed text sent to Claude; reply printed.
3. **Speaks** — reply spoken via Piper. Full loop complete.
4. **Remembers** — conversation context across turns.
5. **Acts** — tools (calendar, notes, etc.).
6. **Draco** — custom cloned voice adapter.

## Source Control

Git repository with `main` protected; one feature branch per milestone; ADRs and this document versioned alongside the code.
