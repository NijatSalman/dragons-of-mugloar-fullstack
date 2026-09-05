package com.company.dragonsofmugloar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Title and description shown on the Swagger UI page. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI dragonsOfMugloarApi() {
        return new OpenAPI().info(new Info()
                .title("Dragons of Mugloar")
                .version("v1")
                .description("Play the Dragons of Mugloar game: start a game, pick ads with the help of our "
                        + "recommendations, buy items, and let the dragon play by itself."));
    }
}
