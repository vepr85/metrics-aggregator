package spongecell.backend.metricsagregatator.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import spongecell.backend.metricsagregatator.configuration.RestConfiguration;
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

    private final RestConfiguration rest;
    @Getter
    private Map<Integer, String> brands = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        initBrands();
    }

    private void initBrands() {
        final ParameterizedTypeReference<List<BrandDTO>> type = new ParameterizedTypeReference<List<BrandDTO>>() {
        };
        final List<BrandDTO> body = rest
                .restTemplate()
                .exchange("/v1/brands", HttpMethod.GET, null, type)
                .getBody();

        Optional<List<BrandDTO>> response = Optional.ofNullable(body);
        response.ifPresent(brandDTOS -> brands = brandDTOS
                .stream()
                .collect(toMap(BrandDTO::getId, BrandDTO::getName)));
    }
}
