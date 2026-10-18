package com.solaria.persistence.service.order;

import com.solaria.persistence.dto.response.order.SaleResponseDTO;
import com.solaria.persistence.repository.order.OrderItemRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesService {

    private final OrderItemRepository orderItemRepository;
    private final RbacAuthorizationService rbac;

    public SalesService(OrderItemRepository orderItemRepository, RbacAuthorizationService rbac) {
        this.orderItemRepository = orderItemRepository;
        this.rbac = rbac;
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> list() {
        return orderItemRepository
                .findBySupplier_Company_IdOrderByOrder_CreatedAtDesc(rbac.getCurrentCompanyId()).stream()
                .map(item -> new SaleResponseDTO(item.getOrder().getId(), item.getId(), item.getOrder().getStatus(),
                        item.getOrder().getCreatedAt(), item.getOrder().getBuyer().getUsername(),
                        item.getOffer() == null ? null : item.getOffer().getId(), item.getOfferSlug(),
                        item.getModelName(), item.getNetUnitPrice(), item.getQuantity(), item.getLineTotal()))
                .toList();
    }
}
