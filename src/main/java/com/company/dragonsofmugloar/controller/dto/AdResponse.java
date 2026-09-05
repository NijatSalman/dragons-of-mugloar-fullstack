package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.AdRecommendation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An ad from the board together with our recommendation")
public record AdResponse(
        @Schema(example = "DSAUBsXa") String adId,
        @Schema(example = "Help Majid Desprez to transport a magic beer mug to steppe in Falldean") String message,
        @Schema(example = "21") int reward,
        @Schema(description = "Turns left before the ad disappears", example = "7") int expiresIn,
        @Schema(description = "The game server's own wording", example = "Quite likely") String probability,
        @Schema(description = "Estimated chance of success, 0..1", example = "0.8") double successChance,
        @Schema(description = "Reward weighted by the chance of success", example = "16.8") double expectedValue,
        @Schema(description = "Whether we advise taking this ad") boolean recommended) {

    public static AdResponse from(AdRecommendation recommendation) {
        var ad = recommendation.ad();
        return new AdResponse(ad.adId(), ad.message(), ad.reward(), ad.expiresIn(), ad.probability().label(),
                recommendation.successChance(), recommendation.expectedValue(), recommendation.recommended());
    }
}
