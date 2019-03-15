package spongecell.backend.metricsagregatator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by abyakimenko on 13.03.2019.
 */
@Data
@Component
@ConfigurationProperties(prefix = "httpclient")
public class HttpSettings {
    private Integer connectTimeout;
    private Integer connectRequestTimeout;    
    private Integer maxPoolSize;
    private Integer readTimeout;
    private Integer keepAliveTime;
    private Integer connectionTTL;
    private Boolean bufferRequestBody;
}
