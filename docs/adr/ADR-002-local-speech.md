# ADR-002: Local speech (whisper.cpp for STT, Piper for TTS)

- **Status:** Accepted
- **Date:** 2026-10-25

## Context

Every turn passes through speech-to-text and text-to-speech. Cloud speech services would add a network round trip to both ends of each turn, charge per use, and send recordings from inside the home to a third party.

## Decision

Run both speech stages locally:

- **Speech-to-text:** whisper.cpp, through a JNI binding or as a local process.
- **Text-to-speech:** Piper.

## Trade-offs accepted

- Model files have to be downloaded, stored and kept up to date on the machine.
- Transcription speed and accuracy depend on the local hardware and on the model size chosen. A bigger model is more accurate but slower.
- Neither engine has an official Java SDK, so each adapter needs JNI or process-management code (see ADR-001).
- Piper speaks with its stock voices only. The planned Draco voice needs a separate voice-cloning adapter (for example XTTS).
