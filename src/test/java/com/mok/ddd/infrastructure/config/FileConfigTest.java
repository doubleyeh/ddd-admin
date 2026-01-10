package com.mok.ddd.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FileConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void fileConfigIsLoaded() {
        contextRunner.withUserConfiguration(FileConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(FileConfig.class);
                });
    }
}