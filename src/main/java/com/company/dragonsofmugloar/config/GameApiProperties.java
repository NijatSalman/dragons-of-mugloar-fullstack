package com.company.dragonsofmugloar.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Location of the game server. HTTP timeouts come from the standard {@code spring.http.clients.*} properties. */
@Validated
@ConfigurationProperties(prefix = "game-api")
public record GameApiProperties(@NotBlank String baseUrl) {
}
