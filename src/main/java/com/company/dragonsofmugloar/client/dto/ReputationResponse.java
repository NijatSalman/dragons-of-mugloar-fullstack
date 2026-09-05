package com.company.dragonsofmugloar.client.dto;

/** Wire format of the game server, not used outside the client package. */
public record ReputationResponse(double people, double state, double underworld) {
}
