# ADR-001: Java for v1

- **Status:** Accepted
- **Date:** 2026-10-25

## Context

Jarvis is a long-running process on a personal machine. It has to drive a microphone and speaker, call native speech engines, and talk to an HTTP API. Of the v1 engines, Picovoice Porcupine ships a Java SDK and Anthropic publishes a Java SDK for the Claude API. The speech engines (whisper.cpp, Piper) are native code and have no official Java SDK.

Reem has 10+ years of Java/JEE experience. Building v1 in her strongest language keeps the focus on architecture and design rather than on learning a new language.

## Decision

Write v1 in Java 25, built with Maven as a multi-module project (`jarvis-core`, `jarvis-adapters`, `jarvis-app`).

## Trade-offs accepted

- Python has the larger speech and ML ecosystem. In Java, whisper.cpp and Piper have to be reached through JNI bindings or run as local processes, which is extra integration work.
- Java's audio API (`javax.sound.sampled`) is low-level, so recording until silence has to be written by hand.
- The JVM uses more memory and starts more slowly than a native binary. That's acceptable for a process that starts once and keeps listening.