package com.solaria.persistence.service.order;

import com.solaria.persistence.config.MarketplaceProperties;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.order.Cart;
import com.solaria.persistence.domain.entity.order.CartItem;
import com.solaria.persistence.domain.entity.order.OrderItem;
import com.solaria.persistence.domain.entity.order.OrderPayment;
import com.solaria.persistence.domain.entity.order.PurchaseOrder;
import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.domain.enums.order.OrderPaymentStatus;
import com.solaria.persistence.domain.enums.order.OrderStatus;
import com.solaria.persistence.dto.response.order.OrderResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InsufficientStockException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.order.CartItemRepository;
import com.solaria.persistence.repository.order.CartRepository;
import com.solaria.persistence.repository.order.OrderItemRepository;
import com.solaria.persistence.repository.order.OrderPaymentRepository;
import com.solaria.persistence.repository.order.PurchaseOrderRepository;
import com.solaria.persistence.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final PurchaseOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OfferRepository offerRepository;
    private final CurrentUserService currentUserService;
    private final PurchaseEligibilityService eligibility;
    private final MarketplaceProperties properties;

    public OrderService(PurchaseOrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderPaymentRepository orderPaymentRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        OfferRepository offerRepository,
                        CurrentUserService currentUserService,
                        PurchaseEligibilityService eligibility,
                        MarketplaceProperties properties) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.offerRepository = offerRepository;
        this.currentUserService = currentUserService;
        this.eligibility = eligibility;
        this.properties = properties;
    }

    @Transactional
    public OrderResponseDTO checkout(PaymentMethod method) {
        User user = currentUserService.getCurrentUser();
        Set<UUID> companies = eligibility.requireBuyer(user);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessRuleException("O carrinho está vazio"));
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByAddedAtAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessRuleException("O carrinho está vazio");
        }

        Map<UUID, Offer> locked = lockOffers(cartItems.stream().map(item -> item.getOffer().getId()).toList());
        List<Map<String, Object>> shortages = new ArrayList<>();
        for (CartItem item : cartItems) {
            Offer offer = locked.get(item.getOffer().getId());
            eligibility.requireNotOwnOffer(companies, offer);
            if (!CartService.isPurchasable(offer) || offer.getAvailability() < item.getQuantity()) {
                shortages.add(Map.of("offerId", offer.getId(), "requested", item.getQuantity(),
                        "available", CartService.isPurchasable(offer) ? offer.getAvailability() : 0));
            }
        }
        if (!shortages.isEmpty()) {
            throw new InsufficientStockException("Estoque insuficiente para itens do carrinho", shortages);
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setBuyer(user);
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Offer offer = locked.get(cartItem.getOffer().getId());
            BigDecimal net = OrderPricing.netUnitPrice(offer.getUnitPrice(), offer.getDiscountPercentage());
            OrderItem item = new OrderItem();
            item.setOffer(offer);
            item.setSupplier(offer.getSupplier());
            item.setOfferSlug(offer.getSlug());
            item.setModelName(offer.getModel().getModel());
            item.setUnitPrice(offer.getUnitPrice());
            item.setDiscountPercentage(offer.getDiscountPercentage() == null
                    ? BigDecimal.ZERO : offer.getDiscountPercentage());
            item.setNetUnitPrice(net);
            item.setQuantity(cartItem.getQuantity());
            item.setLineTotal(OrderPricing.lineTotal(net, cartItem.getQuantity()));
            items.add(item);
            subtotal = subtotal.add(item.getLineTotal());
            offer.setAvailability(offer.getAvailability() - cartItem.getQuantity());
        }
        BigDecimal fee = OrderPricing.fee(subtotal, properties.feePercentage());
        order.setSubtotal(subtotal);
        order.setFeePercentage(properties.feePercentage());
        order.setFeeAmount(fee);
        order.setTotal(subtotal.add(fee));
        order = orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(items);
        offerRepository.saveAll(locked.values());

        OrderPayment payment = new OrderPayment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setAmount(order.getTotal());
        payment.setStatus(OrderPaymentStatus.PAID);
        payment.setPaidAt(OffsetDateTime.now());
        orderPaymentRepository.save(payment);

        cartItemRepository.deleteByCartId(cart.getId());
        return toResponse(order, payment, items);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> list() {
        User user = currentUserService.getCurrentUser();
        List<PurchaseOrder> orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId());
        if (orders.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = orders.stream().map(PurchaseOrder::getId).toList();
        Map<UUID, List<OrderItem>> items = orderItemRepository.findByOrderIdIn(ids).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
        Map<UUID, OrderPayment> payments = orderPaymentRepository.findByOrderIdIn(ids).stream()
                .collect(Collectors.toMap(payment -> payment.getOrder().getId(), Function.identity()));
        return orders.stream()
                .map(order -> toResponse(order, payments.get(order.getId()),
                        items.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO get(UUID id) {
        User user = currentUserService.getCurrentUser();
        PurchaseOrder order = findOwned(id, user, false);
        return toResponse(order, orderPaymentRepository.findByOrderId(id).orElse(null),
                orderItemRepository.findByOrderId(id));
    }

    @Transactional
    public OrderResponseDTO cancel(UUID id) {
        User user = currentUserService.getCurrentUser();
        PurchaseOrder order = findOwned(id, user, true);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessRuleException("Só é possível cancelar um pedido pago");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        Map<UUID, Integer> returned = new HashMap<>();
        for (OrderItem item : items) {
            if (item.getOffer() != null) {
                returned.merge(item.getOffer().getId(), item.getQuantity(), Integer::sum);
            }
        }
        Map<UUID, Offer> locked = lockOffers(new ArrayList<>(returned.keySet()));
        locked.values().forEach(offer -> offer.setAvailability(offer.getAvailability() + returned.get(offer.getId())));
        offerRepository.saveAll(locked.values());

        OffsetDateTime now = OffsetDateTime.now();
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(now);
        order.setCanceledBy(user.getId());
        orderRepository.save(order);

        OrderPayment payment = orderPaymentRepository.findByOrderId(id).orElse(null);
        if (payment != null) {
            payment.setStatus(OrderPaymentStatus.REFUNDED);
            payment.setRefundedAt(now);
            orderPaymentRepository.save(payment);
        }
        return toResponse(order, payment, items);
    }

    private PurchaseOrder findOwned(UUID id, User user, boolean forUpdate) {
        PurchaseOrder order = (forUpdate ? orderRepository.lockById(id) : orderRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com ID: " + id));
        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("O objeto da operação não foi encontrado.");
        }
        return order;
    }

    private Map<UUID, Offer> lockOffers(List<UUID> offerIds) {
        Map<UUID, Offer> locked = new HashMap<>();
        offerIds.stream().distinct().sorted(Comparator.naturalOrder()).forEach(offerId -> locked.put(offerId,
                offerRepository.lockById(offerId).orElseThrow(
                        () -> new ResourceNotFoundException("Oferta não encontrada com ID: " + offerId))));
        return locked;
    }

    private OrderResponseDTO toResponse(PurchaseOrder order, OrderPayment payment, List<OrderItem> items) {
        OrderResponseDTO.Payment paymentView = payment == null ? null : new OrderResponseDTO.Payment(
                payment.getMethod(), payment.getAmount(), payment.getStatus(), payment.getPaidAt(),
                payment.getRefundedAt());
        List<OrderResponseDTO.Line> lines = items.stream()
                .map(item -> new OrderResponseDTO.Line(item.getOffer() == null ? null : item.getOffer().getId(),
                        item.getOfferSlug(), item.getSupplier().getCompany().getTradeName(), item.getModelName(),
                        item.getUnitPrice(), item.getDiscountPercentage(), item.getNetUnitPrice(),
                        item.getQuantity(), item.getLineTotal()))
                .toList();
        return new OrderResponseDTO(order.getId(), order.getStatus(), order.getSubtotal(), order.getFeePercentage(),
                order.getFeeAmount(), order.getTotal(), order.getCreatedAt(), order.getCanceledAt(), paymentView,
                lines);
    }
}
