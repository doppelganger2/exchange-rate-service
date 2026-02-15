package com.example.exchangerateservice.controller;

import com.example.exchangerateservice.api.ExchangeRateApi;
import com.example.exchangerateservice.dto.ConversionResult;
import com.example.exchangerateservice.dto.MultiConversionResult;
import com.example.exchangerateservice.dto.RateResult;
import com.example.exchangerateservice.dto.response.AllRatesResponse;
import com.example.exchangerateservice.dto.response.ConversionResponse;
import com.example.exchangerateservice.dto.response.MultiConversionResponse;
import com.example.exchangerateservice.dto.response.RateResponse;
import com.example.exchangerateservice.mapper.ExchangeRateResponseMapper;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
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
@RequestMapping("/rates")
public class ExchangeRateController implements ExchangeRateApi {

    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateResponseMapper mapper;

    public ExchangeRateController(ExchangeRateService exchangeRateService, ExchangeRateResponseMapper mapper) {
        this.exchangeRateService = exchangeRateService;
        this.mapper = mapper;
    }

    @Override
    @GetMapping("/{from}/{to}")
    public RateResponse getRate(
            @PathVariable Currency from,
            @PathVariable Currency to,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "false") boolean fallback) {
        ExchangeRateProviderType providerType = provider != null ? ExchangeRateProviderType.fromId(provider) : null;
        RateResult result = exchangeRateService.getRate(from, to, providerType, fallback);
        return mapper.toRateResponse(result.data(), from, to, result.rate());
    }

    @Override
    @GetMapping("/{from}")
    public AllRatesResponse getAllRates(
            @PathVariable Currency from,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "false") boolean fallback) {
        ExchangeRateProviderType providerType = provider != null ? ExchangeRateProviderType.fromId(provider) : null;
        return mapper.toAllRatesResponse(exchangeRateService.getAllRates(from, providerType, fallback));
    }

    @Override
    @GetMapping("/convert")
    public ConversionResponse convert(
            @RequestParam Currency from,
            @RequestParam Currency to,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "false") boolean fallback) {
        ExchangeRateProviderType providerType = provider != null ? ExchangeRateProviderType.fromId(provider) : null;
        ConversionResult result = exchangeRateService.convert(from, to, amount, providerType, fallback);
        return mapper.toConversionResponse(result.data(), from, to, amount, result.result());
    }

    @Override
    @GetMapping("/convert/bulk")
    public MultiConversionResponse convertBulk(
            @RequestParam Currency from,
            @RequestParam List<Currency> to,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false, defaultValue = "false") boolean fallback) {
        ExchangeRateProviderType providerType = provider != null ? ExchangeRateProviderType.fromId(provider) : null;
        MultiConversionResult result = exchangeRateService.convertToMultiple(from, to, amount, providerType, fallback);
        return mapper.toMultiConversionResponse(result.data(), from, amount, result.results());
    }
}
