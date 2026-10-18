package com.solaria.persistence.service.order;

import com.solaria.persistence.config.MarketplaceProperties;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.order.Cart;
import com.solaria.persistence.domain.entity.order.CartItem;
import com.solaria.persistence.domain.enums.catalog.OfferStatus;
import com.solaria.persistence.dto.response.order.CartResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InsufficientStockException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.order.CartItemRepository;
import com.solaria.persistence.repository.order.CartRepository;
import com.solaria.persistence.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OfferRepository offerRepository;
    private final CurrentUserService currentUserService;
    private final PurchaseEligibilityService eligibility;
    private final MarketplaceProperties properties;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       OfferRepository offerRepository,
                       CurrentUserService currentUserService,
                       PurchaseEligibilityService eligibility,
                       MarketplaceProperties properties) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.offerRepository = offerRepository;
        this.currentUserService = currentUserService;
        this.eligibility = eligibility;
        this.properties = properties;
    }

    @Transactional
    public CartResponseDTO get() {
        User user = currentUserService.getCurrentUser();
        eligibility.requireBuyer(user);
        return toResponse(findOrCreate(user));
    }

    @Transactional
    public CartResponseDTO putItem(UUID offerId, int quantity) {
        User user = currentUserService.getCurrentUser();
        Set<UUID> companies = eligibility.requireBuyer(user);
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta não encontrada com ID: " + offerId));
        eligibility.requireNotOwnOffer(companies, offer);
        if (!isPurchasable(offer)) {
            throw new BusinessRuleException("A oferta não está disponível para compra");
        }
        if (quantity > offer.getAvailability()) {
            throw new InsufficientStockException("Estoque insuficiente para a quantidade pedida", List.of(Map.of(
                    "offerId", offer.getId(), "requested", quantity, "available", offer.getAvailability())));
        }
        Cart cart = findOrCreate(user);
        CartItem item = cartItemRepository.findByCartIdAndOfferId(cart.getId(), offerId).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setCart(cart);
            created.setOffer(offer);
            return created;
        });
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        touch(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponseDTO removeItem(UUID offerId) {
        User user = currentUserService.getCurrentUser();
        eligibility.requireBuyer(user);
        Cart cart = findOrCreate(user);
        cartItemRepository.findByCartIdAndOfferId(cart.getId(), offerId).ifPresent(item -> {
            cartItemRepository.delete(item);
            touch(cart);
        });
        return toResponse(cart);
    }

    static boolean isPurchasable(Offer offer) {
        return offer.getCommercialStatus() == OfferStatus.ACTIVE
                && offer.getAvailability() != null && offer.getAvailability() > 0
                && (offer.getExpirationDate() == null || offer.getExpirationDate().isAfter(OffsetDateTime.now()));
    }

    private Cart findOrCreate(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private void touch(Cart cart) {
        cart.setUpdatedAt(OffsetDateTime.now());
        cartRepository.save(cart);
    }

    private CartResponseDTO toResponse(Cart cart) {
        List<CartResponseDTO.Line> lines = cartItemRepository.findByCartIdOrderByAddedAtAsc(cart.getId()).stream()
                .map(item -> {
                    Offer offer = item.getOffer();
                    BigDecimal net = OrderPricing.netUnitPrice(offer.getUnitPrice(), offer.getDiscountPercentage());
                    boolean purchasable = isPurchasable(offer) && item.getQuantity() <= offer.getAvailability();
                    return new CartResponseDTO.Line(offer.getId(), offer.getSlug(),
                            offer.getSupplier().getCompany().getSlug(), offer.getSupplier().getCompany().getTradeName(),
                            offer.getModel().getModel(), offer.getUnitPrice(),
                            offer.getDiscountPercentage() == null ? BigDecimal.ZERO : offer.getDiscountPercentage(),
                            net, item.getQuantity(), OrderPricing.lineTotal(net, item.getQuantity()),
                            offer.getAvailability(), purchasable);
                })
                .toList();
        BigDecimal subtotal = lines.stream().map(CartResponseDTO.Line::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fee = OrderPricing.fee(subtotal, properties.feePercentage());
        return new CartResponseDTO(lines, subtotal, properties.feePercentage(), fee, subtotal.add(fee));
    }
}
