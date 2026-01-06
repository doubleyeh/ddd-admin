package com.mok.ddd.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public JsonMapper jsonMapper() {
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        longModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        longModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        longModule.addAbstractTypeMapping(
                org.springframework.security.core.GrantedAuthority.class,
                org.springframework.security.core.authority.SimpleGrantedAuthority.class
        );

        return JsonMapper.builder()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .addModule(longModule)
                .build();
    }

    @Bean
    public JsonMapper redisJsonMapper() {
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        longModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        longModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        longModule.addAbstractTypeMapping(
                GrantedAuthority.class,
                SimpleGrantedAuthority.class
        );

        return JsonMapper.builder()
                .addModule(longModule)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .build(),
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();
    }

}
