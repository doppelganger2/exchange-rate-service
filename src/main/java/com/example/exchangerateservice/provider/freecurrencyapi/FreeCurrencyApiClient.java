package com.example.exchangerateservice.provider.freecurrencyapi;

import com.example.exchangerateservice.provider.freecurrencyapi.dto.FreeCurrencyApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "freecurrencyapi",
    url = "${exchange-rate.providers.freecurrencyapi.base-url}"
)
public interface FreeCurrencyApiClient {

    @GetMapping("/latest")
    FreeCurrencyApiResponse getLatestRates(
            @RequestParam("apikey") String apiKey,
            @RequestParam("base_currency") String baseCurrency
    );
}
