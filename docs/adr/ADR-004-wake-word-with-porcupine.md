# ADR-004: Wake word from v1 using Porcupine

- **Status:** Accepted
- **Date:** 2026-10-25

## Context

Without a wake word, Jarvis would need a push-to-talk button, or it would have to transcribe everything said in the room all the time. Running speech-to-text continuously is heavy, and transcribing every conversation is invasive. Building the wake word in from the start also means the main loop has its final shape from the first milestone.

## Decision

Detect the wake word from v1 (milestone 1, Hears) with Picovoice Porcupine. Use its Java SDK and built-in "Jarvis" keyword, running locally. The Picovoice access key comes from an environment variable.

## Trade-offs accepted

- Porcupine is a proprietary engine and needs a Picovoice account and access key, so Jarvis depends on Picovoice's terms.
- The built-in keyword is free to use as is. A custom wake word would have to be trained through Picovoice.
- Milestone 1 takes more work than a push-to-talk prototype would.
