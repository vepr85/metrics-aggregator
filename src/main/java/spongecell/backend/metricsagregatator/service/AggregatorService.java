package spongecell.backend.metricsagregatator.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AggregatorService {

    private final LookupService lookupService;

    private final MetricAggDTO emptyEntity = MetricAggDTO
            .builder()
            .click(0)
            .impression(0)
            .interaction(0)
            .build();

    /**
     * Total sum of each metric collected, grouped by brand, sorted by total impressions descending.
     *
     * @param src
     *
     * @return List<SummaryByBrandDTO>
     */
    public List<SummaryByBrandDTO> groupByBrand(List<MetricResponseDTO> src) {
        if (src.isEmpty()) {
            return Collections.emptyList();
        }


        List<BrandMetricsDTO> allMetrics = extractAllMetrics(src);
        final Map<Integer, MetricAggDTO> groupedMap = new HashMap<>();

        /*allMetrics
                .stream()
                .collect(groupingBy(BrandMetricsDTO::getBrandId))
                .entrySet()
                .stream()
                .map((entry) -> {
                    final Integer brandId = entry.getKey();
                    final List<BrandMetricsDTO> metricList = entry.getValue();

                    final MetricAggDTO aggDTO = emptyEntity;
                    metricList
                            .stream()
                            .map(metric -> {
                                if (metric.getMetrics().size() == 3) {
                                    final MetricAggDTO metricAggDTO = aggregateMetricByBrand(metric);
                                } else {
                                    return;
                                }
                            });

                    Map.Entry<Integer, MetricAggDTO> entry = new AbstractMap.SimpleImmutableEntry<>();
                    return new EntrySet.;
                })
                .collect(toMap())*/
        ;


        allMetrics
                .stream()
                .collect(groupingBy(BrandMetricsDTO::getBrandId))

                .forEach((brandId, brandMetrics) -> {
                    final MetricAggDTO aggDTO = emptyEntity;
                    brandMetrics.forEach(metric -> aggregateMetricByBrand(aggDTO, metric));
                    groupedMap.put(brandId, aggDTO);
                });

        final Map<Integer, MetricAggDTO> finalMap = groupedMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(MetricAggDTO::getImpression).reversed()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return finalMap.entrySet().stream()
                .map(entry -> SummaryByBrandDTO.builder()
                        .brandId(entry.getKey())
                        .brandName(lookupService.getBrands().get(entry.getKey()))
                        .metrics(entry.getValue())
                        .build())
                .collect(toList());
    }

    /**
     * Total sum of each metric collected, grouped by datetime (rounded to the hour) and brand, sorted by datetime,
     * then brand name, ascending, i.e.
     *
     * @param src
     *
     * @return List<SummaryByDateBrandDTO>
     */
    public List<SummaryByDateBrandDTO> groupByDateBrand(List<MetricResponseDTO> src) {
        if (src.isEmpty()) {
            return Collections.emptyList();
        }

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
                        final MetricAggDTO aggDTO = emptyEntity;
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
                                .brandName(lookupService.getBrands().get(entryL.getKey()))
                                .dateTime(entryH.getKey())
                                .metrics(entryL.getValue())
                                .build())
                )
                .flatMap(Function.identity())
                .collect(toList());
    }

    /**
     * Total sum of the metric counts, grouped by datetime (rounded to the hour), sorted by datetime ascending, i.e.
     *
     * @param src
     *
     * @return List<SummaryByDateTimeOnlyDTO>
     */
    public List<SummaryByDateTimeOnlyDTO> groupByDateTime(List<MetricResponseDTO> src) {
        if (src.isEmpty()) {
            return Collections.emptyList();
        }

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

    /**
     * Total sum of the metric counts collected, grouped by metric
     *
     * @param src
     *
     * @return MetricAggDTO
     */
    public MetricAggDTO groupByMetric(List<MetricResponseDTO> src) {
        if (src.isEmpty()) {
            return emptyEntity;
        }

        final MetricAggDTO reslt = new MetricAggDTO();
        Map<MetricType, Long> metricsMap = src
                .stream()
                .map(MetricResponseDTO::getBrandMetrics)
                .flatMap(Collection::stream)
                .map(BrandMetricsDTO::getMetrics)
                .flatMap(Collection::stream)
                .collect(groupingBy(MetricDTO::getMetric, summingLong(MetricDTO::getCount)));

        reslt.setInteraction(metricsMap.get(MetricType.INTERACTION));
        reslt.setImpression(metricsMap.get(MetricType.IMPRESSION));
        reslt.setClick(metricsMap.get(MetricType.CLICK));

        return reslt;
    }

    private void aggregateMetricByBrand(MetricAggDTO aggDTO, BrandMetricsDTO brandM) {
        if (brandM.getMetrics().size() == 3) {
            brandM.getMetrics().forEach(metric -> calculateMetric(aggDTO, metric));
        }
    }

    private MetricAggDTO aggregateMetricByBrand(BrandMetricsDTO brandM) {
        MetricAggDTO aggDTO = MetricAggDTO.emptyEntity();
        brandM.getMetrics().forEach(metric -> calculateMetric(aggDTO, metric));
        return aggDTO;
    }

    private void calculateMetric(MetricAggDTO aggDTO, MetricDTO metric) {
        switch (metric.getMetric()) {
            case CLICK:
                aggDTO.summClick(metric.getCount());
                break;
            case INTERACTION:
                aggDTO.summInteraction(metric.getCount());
                break;
            case IMPRESSION:
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
