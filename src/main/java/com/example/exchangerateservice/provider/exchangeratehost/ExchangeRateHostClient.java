package com.example.exchangerateservice.provider.exchangeratehost;

import com.example.exchangerateservice.provider.exchangeratehost.dto.ExchangeRateHostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "exchangerate-host",
    url = "${exchangerate.host.base-url}"
)
public interface ExchangeRateHostClient {

    @GetMapping("/live")
    ExchangeRateHostResponse getLatestRates(
        @RequestParam("access_key") String accessKey,
        @RequestParam("source") String source
    );
}
