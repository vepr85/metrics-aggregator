package spongecell.backend.metricsagregatator.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;


/**
 * Created by abyakimenko on 29.06.2019.
 */
@Configuration
public class JaksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer configSerializer() {
        return builder -> {
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);

            builder.featuresToEnable(
                    JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN,
                    MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS,
                    DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
                    DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
            );

            builder.featuresToDisable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            );
        };
    }

    @Bean
    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(jacksonBuilder().build());
        return jsonConverter;
    }

    private Jackson2ObjectMapperBuilder jacksonBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .modules(new JavaTimeModule())
                .featuresToEnable(
                        SerializationFeature.INDENT_OUTPUT,
                        SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,
                        DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                        DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
                        MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .featuresToDisable(
                        SerializationFeature.FAIL_ON_EMPTY_BEANS,
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
