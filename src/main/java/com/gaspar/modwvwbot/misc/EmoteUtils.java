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

    /**
     * Create an animated custom emote that display in Discord chat.
     * @param name Emote name.
     * @param id Emote id.
     * @return The emote string.
     */
    public static String animatedEmote(String name, long id) {
        return "<a:" + name + ":" + id + ">";
    }
}
