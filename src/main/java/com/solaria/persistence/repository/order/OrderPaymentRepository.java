package com.solaria.persistence.repository.order;

import com.solaria.persistence.domain.entity.order.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, UUID> {

    Optional<OrderPayment> findByOrderId(UUID orderId);

    List<OrderPayment> findByOrderIdIn(List<UUID> orderIds);
}
