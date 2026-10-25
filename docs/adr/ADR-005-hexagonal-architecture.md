# ADR-005: Hexagonal architecture (ports and adapters)

- **Status:** Accepted
- **Date:** 2026-10-25

## Context

Every external piece of Jarvis (wake word, recording, speech-to-text, brain, text-to-speech) is a technology choice that's expected to change. The Draco voice, for example, will replace or join Piper. Testing the main loop against a real microphone, native engines and a paid API would be slow, flaky and costly.

## Decision

Use ports and adapters:

- **`jarvis-core`** holds the port interfaces and the `JarvisPipeline` orchestrator, with no external dependencies.
- **`jarvis-adapters`** implements the ports, one package per adapter.
- **`jarvis-app`** wires the adapters into the pipeline and provides `main`.

## Trade-offs accepted

- There are more modules and interfaces than a single-module app would need.
- Adapters can't see each other, so the port contracts have to spell out what they share, such as the audio format and how blocking calls are interrupted.
- Tests of the core rely on hand-written fakes rather than the real engines, so each adapter still needs its own checks against the real thing.
- A port exposes only what every adapter can do. An engine-specific feature, such as streaming transcription, stays hidden until the port is extended.
