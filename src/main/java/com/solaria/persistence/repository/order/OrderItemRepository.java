package com.solaria.persistence.repository.order;

import com.solaria.persistence.domain.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdIn(List<UUID> orderIds);

    List<OrderItem> findByOrderId(UUID orderId);

    List<OrderItem> findBySupplier_Company_IdOrderByOrder_CreatedAtDesc(UUID companyId);
}
