package com.mok.ddd.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void openApiConfigIsLoaded() {
        contextRunner.withUserConfiguration(OpenApiConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenApiConfig.class);
                });
    }
}