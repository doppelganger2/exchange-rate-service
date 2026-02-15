package com.example.exchangerateservice.controller;

import com.example.exchangerateservice.api.ProviderApi;
import com.example.exchangerateservice.dto.response.ProviderResponse;
import com.example.exchangerateservice.provider.ProviderRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProviderController implements ProviderApi {

    private final ProviderRegistry registry;

    public ProviderController(ProviderRegistry registry) {
        this.registry = registry;
    }

    @Override
    @GetMapping("/providers")
    public List<ProviderResponse> listProviders() {
        return registry.getAvailableTypes().stream()
                .map(type -> new ProviderResponse(type.getId(), type.getDisplayName()))
                .toList();
    }
}
