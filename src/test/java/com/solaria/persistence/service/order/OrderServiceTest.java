package com.solaria.persistence.service.order;

import com.solaria.persistence.config.MarketplaceProperties;
import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.order.Cart;
import com.solaria.persistence.domain.entity.order.CartItem;
import com.solaria.persistence.domain.entity.order.OrderItem;
import com.solaria.persistence.domain.entity.order.OrderPayment;
import com.solaria.persistence.domain.entity.order.PurchaseOrder;
import com.solaria.persistence.domain.enums.catalog.OfferStatus;
import com.solaria.persistence.domain.enums.company.PaymentMethod;
import com.solaria.persistence.domain.enums.order.OrderPaymentStatus;
import com.solaria.persistence.domain.enums.order.OrderStatus;
import com.solaria.persistence.dto.response.order.OrderResponseDTO;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.exception.InsufficientStockException;
import com.solaria.persistence.exception.UnauthorizedAccessException;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.order.CartItemRepository;
import com.solaria.persistence.repository.order.CartRepository;
import com.solaria.persistence.repository.order.OrderItemRepository;
import com.solaria.persistence.repository.order.OrderPaymentRepository;
import com.solaria.persistence.repository.order.PurchaseOrderRepository;
import com.solaria.persistence.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock PurchaseOrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock OrderPaymentRepository orderPaymentRepository;
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock OfferRepository offerRepository;
    @Mock CurrentUserService currentUserService;
    @Mock PurchaseEligibilityService eligibility;

    OrderService service;
    User buyer;
    Cart cart;
    Offer offer;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, orderItemRepository, orderPaymentRepository, cartRepository,
                cartItemRepository, offerRepository, currentUserService, eligibility,
                new MarketplaceProperties(new BigDecimal("0.005")));
        buyer = new User();
        buyer.setId(UUID.randomUUID());
        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUser(buyer);

        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setTradeName("Solar Sul");
        Supplier supplier = new Supplier();
        supplier.setCompany(company);
        Model model = new Model();
        model.setModel("Painel 550W");
        offer = new Offer();
        offer.setId(UUID.randomUUID());
        offer.setSupplier(supplier);
        offer.setModel(model);
        offer.setSlug("painel-550w");
        offer.setUnitPrice(new BigDecimal("1000.00"));
        offer.setDiscountPercentage(new BigDecimal("10.00"));
        offer.setAvailability(10);
        offer.setCommercialStatus(OfferStatus.ACTIVE);

        when(currentUserService.getCurrentUser()).thenReturn(buyer);
    }

    private CartItem cartItem(int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setOffer(offer);
        item.setQuantity(quantity);
        return item;
    }

    private void givenCart(int quantity) {
        when(eligibility.requireBuyer(buyer)).thenReturn(Set.of());
        when(cartRepository.findByUserId(buyer.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByAddedAtAsc(cart.getId())).thenReturn(List.of(cartItem(quantity)));
        when(offerRepository.lockById(offer.getId())).thenReturn(Optional.of(offer));
    }

    @Test
    void checkoutDiscountsStockAndChargesBuyerFee() {
        givenCart(3);
        when(orderRepository.save(any(PurchaseOrder.class))).thenAnswer(call -> call.getArgument(0));

        OrderResponseDTO order = service.checkout(PaymentMethod.PIX);

        assertEquals(new BigDecimal("2700.00"), order.subtotal());
        assertEquals(new BigDecimal("13.50"), order.feeAmount());
        assertEquals(new BigDecimal("2713.50"), order.total());
        assertEquals(OrderStatus.PAID, order.status());
        assertEquals(PaymentMethod.PIX, order.payment().method());
        assertEquals(new BigDecimal("900.00"), order.items().get(0).netUnitPrice());
        assertEquals(7, offer.getAvailability());
        verify(cartItemRepository).deleteByCartId(cart.getId());
    }

    @Test
    void checkoutRejectsWhenStockIsShortAndKeepsEverythingUntouched() {
        givenCart(11);

        InsufficientStockException error = assertThrows(InsufficientStockException.class,
                () -> service.checkout(PaymentMethod.PIX));

        assertEquals("INSUFFICIENT_STOCK", error.getErrorCode());
        assertEquals(10, offer.getAvailability());
        verify(orderRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteByCartId(any());
    }

    @Test
    void checkoutRejectsPausedOffers() {
        offer.setCommercialStatus(OfferStatus.PAUSED);
        givenCart(1);

        assertThrows(InsufficientStockException.class, () -> service.checkout(PaymentMethod.BOLETO));
    }

    @Test
    void checkoutRejectsEmptyCart() {
        when(eligibility.requireBuyer(buyer)).thenReturn(Set.of());
        when(cartRepository.findByUserId(buyer.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByAddedAtAsc(cart.getId())).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> service.checkout(PaymentMethod.PIX));
    }

    private PurchaseOrder paidOrder(User owner) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(UUID.randomUUID());
        order.setBuyer(owner);
        order.setStatus(OrderStatus.PAID);
        order.setSubtotal(new BigDecimal("1800.00"));
        order.setFeePercentage(new BigDecimal("0.005"));
        order.setFeeAmount(new BigDecimal("9.00"));
        order.setTotal(new BigDecimal("1809.00"));
        return order;
    }

    @Test
    void cancelReturnsStockAndRefundsPayment() {
        PurchaseOrder order = paidOrder(buyer);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setOffer(offer);
        item.setSupplier(offer.getSupplier());
        item.setQuantity(2);
        item.setOfferSlug("painel-550w");
        item.setModelName("Painel 550W");
        OrderPayment payment = new OrderPayment();
        payment.setOrder(order);
        payment.setMethod(PaymentMethod.PIX);
        payment.setAmount(order.getTotal());
        when(orderRepository.lockById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of(item));
        when(offerRepository.lockById(offer.getId())).thenReturn(Optional.of(offer));
        when(orderPaymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));

        OrderResponseDTO canceled = service.cancel(order.getId());

        assertEquals(OrderStatus.CANCELED, canceled.status());
        assertEquals(OrderPaymentStatus.REFUNDED, canceled.payment().status());
        assertEquals(12, offer.getAvailability());
        assertEquals(buyer.getId(), order.getCanceledBy());
    }

    @Test
    void cancelRejectsAlreadyCanceledOrder() {
        PurchaseOrder order = paidOrder(buyer);
        order.setStatus(OrderStatus.CANCELED);
        when(orderRepository.lockById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(BusinessRuleException.class, () -> service.cancel(order.getId()));
    }

    @Test
    void cancelRejectsOtherBuyersOrder() {
        User stranger = new User();
        stranger.setId(UUID.randomUUID());
        PurchaseOrder order = paidOrder(stranger);
        when(orderRepository.lockById(order.getId())).thenReturn(Optional.of(order));

        assertThrows(UnauthorizedAccessException.class, () -> service.cancel(order.getId()));
    }
}
