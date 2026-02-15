package com.example.exchangerateservice.provider.frankfurter;

import com.example.exchangerateservice.provider.frankfurter.dto.FrankfurterLatestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "frankfurter",
    url = "${exchange-rate.providers.frankfurter.base-url}"
)
public interface FrankfurterClient {

    @GetMapping("/latest")
    FrankfurterLatestResponse getLatestRates(@RequestParam("base") String base);
}
