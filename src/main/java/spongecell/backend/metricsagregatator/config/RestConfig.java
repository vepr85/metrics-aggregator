package spongecell.backend.metricsagregatator.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;

/**
 * Spring config for rest client
 */
@Configuration
@RequiredArgsConstructor
public class RestConfig {

    private final HttpSettings httpSettings;
    private final int MS = 1000;

    @Bean
    public RestTemplate restTemplate() {
        final HttpComponentsClientHttpRequestFactory factory = getHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        replaceConverter(restTemplate);

        return restTemplate;
    }

    @Bean
    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(getObjectMapper());
        return jsonConverter;
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return jacksonBuilder().build();
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

    private HttpComponentsClientHttpRequestFactory getHttpRequestFactory() {
        final HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(closeableHttpClient());

        factory.setConnectionRequestTimeout(httpSettings.getConnectRequestTimeout());
        factory.setConnectTimeout(httpSettings.getConnectTimeout());
        factory.setReadTimeout(httpSettings.getReadTimeout());
        factory.setBufferRequestBody(httpSettings.getBufferRequestBody());
        return factory;
    }

    private CloseableHttpClient closeableHttpClient() {
        RequestConfig.Builder requestBuilder = RequestConfig
                .custom()
                .setConnectTimeout(httpSettings.getConnectTimeout())
                .setSocketTimeout(httpSettings.getConnectTimeout());

        HttpClientBuilder result = HttpClients.custom()
                .setConnectionManager(poolingHttpManager())
                .setKeepAliveStrategy(customKeepAliveStrategy())
                .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                .setRedirectStrategy(new LaxRedirectStrategy());
        result.setDefaultRequestConfig(requestBuilder.build());
        return result.build();
    }

    private ConnectionKeepAliveStrategy customKeepAliveStrategy() {
        return (response, context) -> {
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (Objects.nonNull(value) && "timeout".equalsIgnoreCase(param)) {
                        return Long.parseLong(value) * MS;
                    }
                }
                return httpSettings.getKeepAliveTime();
            };
    }

    private PoolingHttpClientConnectionManager poolingHttpManager() {
        PoolingHttpClientConnectionManager connManager =
                new PoolingHttpClientConnectionManager(httpSettings.getConnectionTTL(), TimeUnit.MILLISECONDS);
        connManager.setDefaultMaxPerRoute(httpSettings.getMaxPoolSize());
        connManager.setMaxTotal(httpSettings.getMaxPoolSize());
        return connManager;
    }

    private void replaceConverter(RestTemplate restTemplate) {
        restTemplate.getMessageConverters().forEach(converter -> {
            if (converter.getClass().isAssignableFrom(MappingJackson2HttpMessageConverter.class)) {
                final int index = restTemplate.getMessageConverters().indexOf(converter);
                restTemplate.getMessageConverters().set(index, customJackson2HttpMessageConverter());
            }
        });
    }
}