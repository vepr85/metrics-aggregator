package spongecell.backend.metricsagregatator.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import spongecell.backend.metricsagregatator.config.MetricSettings;
import spongecell.backend.metricsagregatator.config.RestConfig;
import spongecell.backend.metricsagregatator.dto.BrandDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

/**
 * Service getting extra info from backend
 * <p>
 * Created by abyakimenko on 11.03.2019.
 */
@Service
@RequiredArgsConstructor
public class LookupService implements InitializingBean {

    private final RestConfig rest;
    private final MetricSettings settings;
    @Getter
    private Map<Integer, String> brands = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        initBrands();
    }

    private void initBrands() {
        final String requestUrl = settings.getUrl().concat("/v1/brands");

        final ParameterizedTypeReference<List<BrandDTO>> type = new ParameterizedTypeReference<List<BrandDTO>>() {
        };
        Optional<List<BrandDTO>> response = Optional.ofNullable(rest
                .restTemplate()
                .exchange(requestUrl, HttpMethod.GET, null, type)
                .getBody());

        response.ifPresent(brandDTOS -> brands = brandDTOS
                .stream()
                .collect(toMap(BrandDTO::getId, BrandDTO::getName)));
    }
}
