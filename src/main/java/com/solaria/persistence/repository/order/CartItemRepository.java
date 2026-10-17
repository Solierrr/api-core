package com.solaria.persistence.repository.order;

import com.solaria.persistence.domain.entity.order.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCartIdOrderByAddedAtAsc(UUID cartId);

    Optional<CartItem> findByCartIdAndOfferId(UUID cartId, UUID offerId);

    void deleteByCartId(UUID cartId);
}
