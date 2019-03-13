package spongecell.backend.metricsagregatator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spongecell.backend.metricsagregatator.dto.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/**
 * Aggregation service
 * <p>
 * Created by abyakimenko on 04.03.2019.
 */
@Slf4j
@Service
public class AggregatorService {

    /**
     * Total sum of each metric collected, grouped by brand, sorted by total impressions descending.
     *
     * @param src
     * @return
     */
    public List<SummaryByBrandDTO> groupByBrand(List<MetricResponseDTO> src) {
        if (!src.isEmpty()) {

            List<BrandMetricsDTO> allMetrics = extractAllMetrics(src);

            final Map<Integer, MetricAggDTO> groupedMap = new HashMap<>();
            allMetrics
                    .stream()
                    .collect(groupingBy(BrandMetricsDTO::getBrandId))
                    .forEach((key, brandMs) -> {
                        int brandId = key;
                        final MetricAggDTO aggDTO = MetricAggDTO.emptyEntity();
                        brandMs.forEach(brandM -> aggregateMetricByBrand(aggDTO, brandM));
                        groupedMap.put(brandId, aggDTO);
                    });

            final Map<Integer, MetricAggDTO> finalMap = groupedMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.comparing(MetricAggDTO::getImpression).reversed()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            return finalMap.entrySet().stream()
                    .map(entry -> SummaryByBrandDTO.builder()
                            .brandId(entry.getKey())
                            .brandName(LookupService.getBrands().get(entry.getKey()))
                            .metrics(entry.getValue())
                            .build())
                    .collect(toList());
        }

        return new ArrayList<>(0);
    }

    /**
     * Total sum of each metric collected, grouped by datetime (rounded to the hour) and brand, sorted by datetime,
     * then brand name, ascending, i.e.
     *
     * @param src
     * @return
     */
    public List<SummaryByDateBrandDTO> groupByDateBrand(List<MetricResponseDTO> src) {
        if (!src.isEmpty()) {
            List<BrandMetricsDTO> allMetrics = extractAllMetrics(src);

            // collecting map sorting by keys (datetime and brandId)
            final Map<ZonedDateTime, Map<Integer, MetricAggDTO>> finalMap = new TreeMap<>();
            allMetrics
                    .stream()
                    .collect(groupingBy(x -> x.getDateTime().truncatedTo(ChronoUnit.HOURS),
                            groupingBy(BrandMetricsDTO::getBrandId)))
                    .forEach((date, brandsMap) -> {
                        Map<Integer, MetricAggDTO> mapTree = new TreeMap<>();
                        brandsMap.forEach((brandId, brandMs) -> {
                            final MetricAggDTO aggDTO = MetricAggDTO.emptyEntity();
                            brandMs.forEach(brandM -> aggregateMetricByBrand(aggDTO, brandM));
                            mapTree.put(brandId, aggDTO);
                        });
                        finalMap.put(date, mapTree);
                    });

            // extracting list from our map
            return finalMap.entrySet()
                    .stream()
                    .map(entryH -> entryH.getValue()
                            .entrySet()
                            .stream()
                            .map(entryL -> SummaryByDateBrandDTO
                                    .builder()
                                    .brandId(entryL.getKey())
                                    .brandName(LookupService.getBrands().get(entryL.getKey()))
                                    .dateTime(entryH.getKey())
                                    .metrics(entryL.getValue())
                                    .build())
                    )
                    .flatMap(Function.identity())
                    .collect(toList());
        }

        return Collections.emptyList();
    }

    /**
     * Total sum of the metric counts, grouped by datetime (rounded to the hour), sorted by datetime ascending, i.e.
     *
     * @param src
     * @return
     */
    public List<SummaryByDateTimeOnlyDTO> groupByDateTime(List<MetricResponseDTO> src) {
        if (!src.isEmpty()) {
            List<BrandMetricsDTO> allMetrics = extractAllMetrics(src);

            return allMetrics
                    .stream()
                    .collect(groupingBy(x -> x.getDateTime().truncatedTo(ChronoUnit.HOURS)))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        final MetricAggDTO aggDTO = MetricAggDTO.builder().build();
                        entry.getValue()
                                .stream()
                                .map(BrandMetricsDTO::getMetrics)
                                .flatMap(Collection::stream)
                                .forEach(metric -> calculateMetric(aggDTO, metric));

                        return SummaryByDateTimeOnlyDTO
                                .builder()
                                .dateTime(entry.getKey())
                                .metrics(aggDTO)
                                .build();
                    })
                    .sorted(Comparator.comparing(SummaryByDateTimeOnlyDTO::getDateTime))
                    .collect(toList());
        }

        return Collections.emptyList();
    }

    /**
     * Total sum of the metric counts collected, grouped by metric
     *
     * @param src
     * @return
     */
    public MetricAggDTO groupByMetric(List<MetricResponseDTO> src) {
        if (!src.isEmpty()) {
            final MetricAggDTO reslt = new MetricAggDTO();
            Map<MetricType, Long> metricsMap = src
                    .stream()
                    .map(MetricResponseDTO::getBrandMetrics)
                    .flatMap(Collection::stream)
                    .map(BrandMetricsDTO::getMetrics)
                    .flatMap(Collection::stream)
                    .collect(groupingBy(x -> MetricType.valueOf(x.getMetric().toUpperCase()),
                            summingLong(MetricDTO::getCount)));

            reslt.setInteraction(metricsMap.get(MetricType.INTERACTION));
            reslt.setImpression(metricsMap.get(MetricType.IMPRESSION));
            reslt.setClick(metricsMap.get(MetricType.CLICK));

            return reslt;
        }

        return MetricAggDTO.emptyEntity();
    }

    private void aggregateMetricByBrand(MetricAggDTO aggDTO, BrandMetricsDTO brandM) {
        if (brandM.getMetrics().size() == 3) {
            brandM.getMetrics().forEach(metric -> calculateMetric(aggDTO, metric));
        }
    }

    private void calculateMetric(MetricAggDTO aggDTO, MetricDTO metric) {
        switch (metric.getMetric()) {
            case "click":
                aggDTO.summClick(metric.getCount());
                break;
            case "interaction":
                aggDTO.summInteraction(metric.getCount());
                break;
            case "impression":
                aggDTO.summImpression(metric.getCount());
                break;
        }
    }

    private List<BrandMetricsDTO> extractAllMetrics(List<MetricResponseDTO> src) {
        return src
                .stream()
                .map(MetricResponseDTO::getBrandMetrics)
                .flatMap(Collection::stream)
                .collect(toList());
    }
}
