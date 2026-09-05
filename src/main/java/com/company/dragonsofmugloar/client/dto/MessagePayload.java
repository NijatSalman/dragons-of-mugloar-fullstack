package com.company.dragonsofmugloar.client.dto;

/**
 * One ad exactly as the game server sends it. {@code encrypted} is {@code null} for plain text,
 * {@code 1} for Base64 and {@code 2} for ROT13; when set, {@code adId}, {@code message} and
 * {@code probability} are encoded.
 */
public record MessagePayload(String adId, String message, int reward, int expiresIn, Integer encrypted,
                              String probability) {
}
