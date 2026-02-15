package com.example.exchangerateservice.controller;

import com.example.exchangerateservice.api.ExchangeRateApi;
import com.example.exchangerateservice.dto.response.AllRatesResponse;
import com.example.exchangerateservice.dto.response.ConversionResponse;
import com.example.exchangerateservice.dto.response.MultiConversionResponse;
import com.example.exchangerateservice.dto.response.RateResponse;
import com.example.exchangerateservice.mapper.ExchangeRateResponseMapper;
import com.example.exchangerateservice.service.ExchangeRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ExchangeRateController implements ExchangeRateApi {

    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateResponseMapper mapper;

    public ExchangeRateController(ExchangeRateService exchangeRateService, ExchangeRateResponseMapper mapper) {
        this.exchangeRateService = exchangeRateService;
        this.mapper = mapper;
    }

    @Override
    @GetMapping("/rates/{from}/{to}")
    public RateResponse getRate(@PathVariable Currency from, @PathVariable Currency to) {
        return mapper.toRateResponse(from, to, exchangeRateService.getRate(from, to));
    }

    @Override
    @GetMapping("/rates/{from}")
    public AllRatesResponse getAllRates(@PathVariable Currency from) {
        return mapper.toAllRatesResponse(exchangeRateService.getAllRates(from));
    }

    @Override
    @GetMapping("/convert")
    public ConversionResponse convert(
            @RequestParam Currency from,
            @RequestParam Currency to,
            @RequestParam BigDecimal amount) {
        return mapper.toConversionResponse(from, to, amount, exchangeRateService.convert(from, to, amount));
    }

    @Override
    @GetMapping("/convert/bulk")
    public MultiConversionResponse convertBulk(
            @RequestParam Currency from,
            @RequestParam List<Currency> to,
            @RequestParam BigDecimal amount) {
        return mapper.toMultiConversionResponse(from, amount, exchangeRateService.convertToMultiple(from, to, amount));
    }
}
