package com.company.dragonsofmugloar.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decodes the obfuscated ads the game server occasionally sends: code 1 is Base64, code 2 is ROT13.
 * Unknown codes are passed through unchanged so a new server variant degrades gracefully instead of failing.
 */
final class MessageDecoder {

    static final int BASE64 = 1;
    static final int ROT13 = 2;

    private MessageDecoder() {
    }

    static String decode(String text, Integer encryption) {
        if (text == null || encryption == null) {
            return text;
        }
        return switch (encryption) {
            case BASE64 -> new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
            case ROT13 -> rot13(text);
            default -> text;
        };
    }

    private static String rot13(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                out.append((char) ('a' + (c - 'a' + 13) % 26));
            } else if (c >= 'A' && c <= 'Z') {
                out.append((char) ('A' + (c - 'A' + 13) % 26));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
