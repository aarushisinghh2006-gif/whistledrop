package com.whistledrop;

import com.whistledrop.entity.Moderator;
import com.whistledrop.repository.ModeratorRepository;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class WhistleDropApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhistleDropApplication.class, args);
    }

    // Seed default moderator if none exists
    @Bean
    public CommandLineRunner initDefaultModerator(
            ModeratorRepository moderatorRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.default-moderator.username:moderator}") String defaultUsername,
            @Value("${app.default-moderator.password:password}") String defaultPassword) {
        return args -> {
            if (!moderatorRepository.existsByUsername(defaultUsername)) {
                Moderator moderator = new Moderator(
                        defaultUsername,
                        passwordEncoder.encode(defaultPassword),
                        "ROLE_MODERATOR"
                );
                moderatorRepository.save(moderator);
                System.out.println("Initialized default moderator account: " + defaultUsername);
            }
        };
    }

    // Swagger / OpenAPI documentation configuration
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WhistleDrop API - Speak Without Being Seen")
                        .version("1.0.0")
                        .description("Confidential anonymous reporting backend. Submit reports anonymously without identity capture, and track case progress via secret case codes."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
