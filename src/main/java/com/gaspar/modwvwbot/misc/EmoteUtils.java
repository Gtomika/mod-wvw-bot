package com.gaspar.modwvwbot.misc;

public abstract class EmoteUtils {

    /**
     * Create custom emote string, that displays the emote in the chat.
     * @param name Name of the emote, without : on both sides.
     * @param id Id of the emote.
     */
    public static String customEmote(String name, long id) {
        return "<:" + name + ":" + id + ">";
    }

    public static String defaultEmote(String name) {
        return ":" + name + ":";
    }

}
