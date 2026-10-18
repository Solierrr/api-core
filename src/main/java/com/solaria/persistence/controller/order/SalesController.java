package com.solaria.persistence.controller.order;

import com.solaria.persistence.dto.response.order.SaleResponseDTO;
import com.solaria.persistence.openapi.order.SalesOpenApi;
import com.solaria.persistence.service.order.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController implements SalesOpenApi {

    private final SalesService salesService;

    @Override
    @GetMapping
    public List<SaleResponseDTO> list() {
        return salesService.list();
    }
}
