package spongecell.backend.metricsagregatator.service;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import spongecell.backend.metricsagregatator.config.HttpClientConfig;
import spongecell.backend.metricsagregatator.dto.BrandDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Service getting extra info from backend
 * 
 * Created by abyakimenko on 11.03.2019.
 */
@Service
public class LookupService implements InitializingBean {
    @Getter
    private static Map<Integer, String> brands = new HashMap<>();

    private final HttpClientConfig rest;
    private String metricUrl;
    
    @Autowired
    public LookupService(HttpClientConfig rest, @Value("${metric.url}") String metricUrl) {
        this.rest = rest;
        this.metricUrl = metricUrl;
    }

    private void initBrands() {
        final String requestUrl = metricUrl.concat("/v1/brands");

        final ParameterizedTypeReference<List<BrandDTO>> type = new ParameterizedTypeReference<List<BrandDTO>>() {
        };
        List<BrandDTO> response = rest
                .restTemplate()
                .exchange(requestUrl, HttpMethod.GET, null, type)
                .getBody();

        brands = response.stream().collect(toMap(BrandDTO::getId, BrandDTO::getName));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initBrands();
    }
}
