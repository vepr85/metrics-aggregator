package spongecell.backend.metricsagregatator.config;

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
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Spring config for http client
 */
@Configuration
public class HttpClientConfig {

    private static final int DEFAULT_KEEP_ALIVE_TIME = 5000;
    @Value("${httpclient.connectTimeout:1000}")
    private int connectTimeout;
    @Value("${httpclient.connectRequestTimeout:1000}")
    private int connectRequestTimeout;
    @Value("${httpclient.readTimeout:1000}")
    private int readTimeout;
    @Value("${httpclient.bufferRequestBody:true}")
    private boolean bufferRequestBody;
    @Value("${httpclient.connectionTTL:1000}")
    private int connectionTTL;
    @Value("${httpclient.maxPoolSize:100}")
    private int maxPoolSize;

    @Bean
    public RestTemplate restTemplate() {
        final HttpComponentsClientHttpRequestFactory factory = getHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }

    private HttpComponentsClientHttpRequestFactory getHttpRequestFactory() {
        final HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(closeableHttpClient());
        factory.setConnectionRequestTimeout(connectRequestTimeout);
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        factory.setBufferRequestBody(bufferRequestBody);
        return factory;
    }

    private CloseableHttpClient closeableHttpClient() {
        RequestConfig.Builder requestBuilder = RequestConfig
                .custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(connectTimeout);

        HttpClientBuilder result = HttpClients.custom().setConnectionManager(poolingHttpManager())
                .setKeepAliveStrategy(customKeepAliveStrategy)
                .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                .setRedirectStrategy(new LaxRedirectStrategy());
        result.setDefaultRequestConfig(requestBuilder.build());
        return result.build();
    }

    private PoolingHttpClientConnectionManager poolingHttpManager() {
        PoolingHttpClientConnectionManager connManager =
                new PoolingHttpClientConnectionManager(connectionTTL, TimeUnit.MILLISECONDS);
        connManager.setDefaultMaxPerRoute(maxPoolSize);
        connManager.setMaxTotal(maxPoolSize);
        return connManager;
    }

    private ConnectionKeepAliveStrategy customKeepAliveStrategy = (response, context) -> {
        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (Objects.nonNull(value) && "timeout".equalsIgnoreCase(param)) {
                return Long.parseLong(value) * 1000;
            }
        }
        return DEFAULT_KEEP_ALIVE_TIME;
    };
}