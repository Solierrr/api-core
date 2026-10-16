package com.solaria.persistence.controller.company;

import com.solaria.persistence.dto.response.company.SubscriptionOverviewDTO;
import com.solaria.persistence.openapi.company.SubscriptionOverviewOpenApi;
import com.solaria.persistence.service.company.SubscriptionOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionOverviewController implements SubscriptionOverviewOpenApi {

    private final SubscriptionOverviewService overviewService;

    @Override
    @GetMapping
    public SubscriptionOverviewDTO current() {
        return overviewService.current();
    }
}
