package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.PurchaseResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Outcome of a purchase; the turn advances even when it fails")
public record PurchaseResultResponse(
        boolean success,
        @Schema(example = "70") int gold,
        @Schema(example = "3") int lives,
        @Schema(example = "1") int level,
        @Schema(example = "12") int turn) {

    public static PurchaseResultResponse from(PurchaseResult result) {
        return new PurchaseResultResponse(result.success(), result.gold(), result.lives(), result.level(), result.turn());
    }
}
