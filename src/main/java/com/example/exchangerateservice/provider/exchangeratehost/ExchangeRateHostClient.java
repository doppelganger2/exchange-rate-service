package com.example.exchangerateservice.provider.exchangeratehost;

import com.example.exchangerateservice.provider.exchangeratehost.dto.ExchangeRateHostResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "exchangerate-host",
    url = "${exchange-rate.providers.exchangerate-host.base-url}"
)
@ConditionalOnProperty(prefix = "exchange-rate.providers.exchangerate-host", name = "enabled", havingValue = "true")
@ConditionalOnExpression("!'${exchange-rate.providers.exchangerate-host.access-key:}'.isBlank()")
public interface ExchangeRateHostClient {

    @GetMapping("/live")
    ExchangeRateHostResponse getLatestRates(
        @RequestParam("access_key") String accessKey,
        @RequestParam("source") String source
    );
}
