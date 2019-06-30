package spongecell.backend.metricsagregatator.configuration;

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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import spongecell.backend.metricsagregatator.settings.HttpSettings;
import spongecell.backend.metricsagregatator.settings.MetricSettings;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;


/**
 * Spring config for rest client
 */
@Configuration
@RequiredArgsConstructor
@Import(JaksonConfiguration.class)
public class RestConfiguration {

    private final MetricSettings settings;

    private final HttpSettings httpSettings;

    private final JaksonConfiguration jaksonConfig;

    private final int MS = 1000;

    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate1 = new RestTemplateBuilder()
                .rootUri(settings.getUrl())
                .messageConverters(jaksonConfig.customJackson2HttpMessageConverter())
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .setConnectTimeout(Duration.ofSeconds(httpSettings.getConnectTimeout()))
                .setReadTimeout(Duration.ofSeconds(httpSettings.getReadTimeout()))
                .build();
        restTemplate1.setRequestFactory(getHttpRequestFactory());
        return restTemplate1;
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
}
