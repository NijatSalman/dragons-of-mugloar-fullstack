package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.game.Reputation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How the factions of Mugloar see the player; investigating costs one turn")
public record ReputationResponse(double people, double state, double underworld) {

    public static ReputationResponse from(Reputation reputation) {
        return new ReputationResponse(reputation.people(), reputation.state(), reputation.underworld());
    }
}
