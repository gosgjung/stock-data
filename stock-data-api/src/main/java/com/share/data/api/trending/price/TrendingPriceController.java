package com.share.data.api.trending.price;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.share.data.api.trending.price.convertor.MapProcessor;
import com.share.data.api.trending.price.entity.TrendingPriceEntity;
import com.types.date.localdate.FormatterTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;

@Controller
public class TrendingPriceController {
    final static Logger LOGGER = LoggerFactory.getLogger(TrendingPriceController.class);

    @Autowired
    private TrendingPriceService trendingPriceService;

    @GetMapping(value = "/trending/price/index")
    public String getPage(Model model) throws JsonProcessingException {
        Map<String,Object> params = new HashMap<>();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = LocalDate.parse(
                endDate.minus(5, ChronoUnit.YEARS)
                        .format(FormatterTypes.YYYY0101.ofPattern()),
                FormatterTypes.YYYYMMDD.ofPattern());

        String strStartDate = startDate.format(FormatterTypes.YYYYMMDD.ofPattern());
        String strEndDate = endDate.format(FormatterTypes.YYYYMMDD.ofPattern());

        params.put("startDate", strStartDate);
        params.put("endDate", strEndDate);

//        List<Map<String,Object>> trendingResult = closingPriceService.getTrendingResult(params);
        return "/trending/price/index";
    }

    @GetMapping(value = "/api/trending/default")
    public @ResponseBody Object getDefaultData(@RequestParam(value = "startDate", required = false) String startDate,
                                               @RequestParam(value = "endDate", required = false) String endDate){
        Map<String, Object> params = new HashMap<>();
        LocalDate endLDate = LocalDate.now();
        LocalDate startLDate = endLDate.minus(5, ChronoUnit.YEARS);

        params.put("startDate", startLDate.format(FormatterTypes.YYYY0101.ofPattern()));
        params.put("endDate", endLDate.format(FormatterTypes.YYYYMMDD.ofPattern()));

        List<Map<String, Object>> trendingResult = trendingPriceService.getTrendingResult(params);
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("chart", trendingResult);
        return resultMap;
    }

    @GetMapping(value = "/api/trending/kospi/minmax")
    public @ResponseBody Object getMinMaxKospi( @RequestParam(value = "startDate", required = false) String startDate,
                                                @RequestParam(value = "endDate", required = false) String endDate){
        Map<String, Object> params = new HashMap<>();
        LocalDate endLDate = LocalDate.now();
        LocalDate startLDate = endLDate.minus(5, ChronoUnit.YEARS);

        params.put("startDate", startLDate.format(FormatterTypes.YYYY0101.ofPattern()));
        params.put("endDate", endLDate.format(FormatterTypes.YYYYMMDD.ofPattern()));

        List<Map<String, Object>> trendingResult = trendingPriceService.getTrendingResult(params);
        TrendingPriceEntity max = trendingResult.parallelStream()
                .map(MapProcessor::processMapToEntity)
                .collect(maxBy(comparingDouble(TrendingPriceEntity::getdKospi)))
                .get();

        TrendingPriceEntity min = trendingResult.parallelStream()
                .map(MapProcessor::processMapToEntity)
                .collect(minBy(comparingDouble(TrendingPriceEntity::getdKospi)))
                .get();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("max", max);
        resultMap.put("min", min);
        return resultMap;
    }
}
