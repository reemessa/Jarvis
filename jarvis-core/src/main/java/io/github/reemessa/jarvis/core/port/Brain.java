package io.github.reemessa.jarvis.core.port;

/**
 * Decides what Jarvis says in reply to the user.
 */
public interface Brain {

    /**
     * Produces a reply to what the user said.
     *
     * @param userText the user's transcribed speech
     * @return the reply text
     */
    String respond(String userText);
}
