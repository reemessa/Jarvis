# ADR-003: Claude API as the brain

- **Status:** Accepted
- **Date:** 2026-10-25

## Context

The brain has to understand open-ended speech and reply naturally. Later milestones need it to keep conversation context (Remembers) and to call tools such as a calendar or notes (Acts). Running a model that capable locally takes more hardware than a personal machine usually has.

## Decision

The `Brain` adapter calls the Claude API. The API key is read from an environment variable and never committed.

## Trade-offs accepted

- API usage is billed separately from the Claude subscription.
- The brain is the one stage that needs the network. Without a connection, Jarvis can hear and speak but can't answer.
- The user's transcribed text, though not the audio, leaves the machine.
- Each turn waits on a network round trip to the API.

Because `Brain` is a port (ADR-005), a local model can replace it later without changing the pipeline.
