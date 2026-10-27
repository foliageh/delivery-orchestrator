package com.delivery.service;

import com.delivery.dto.message.RefundMessage;
import com.delivery.exception.ApiException;
import com.delivery.model.Order;
import com.delivery.model.Store;
import com.delivery.repository.OrderRepository;
import com.delivery.security.SecurityService;
import com.delivery.security.UserRole;
import com.delivery.service.jms.JmsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {
    private final OrderRepository orderRepository;
    private final SecurityService securityService;
    private final JmsService jmsService;

    public Store getProfile() {
        return (Store) securityService.getCurrentUser(UserRole.STORE);
    }

    public Order getOrder(Long orderId) {
        var store = (Store) securityService.getCurrentUser(UserRole.STORE);
        return getOrderWithStoreAccess(orderId, store);
    }

    public Page<Order> getOrders(int pageNumber, int pageSize) {
        var store = (Store) securityService.getCurrentUser(UserRole.STORE);
        return orderRepository.findByStore(store, PageRequest.of(pageNumber, pageSize));
    }

    public Order collectOrder(Long orderId) {
        var store = (Store) securityService.getCurrentUser(UserRole.STORE);
        var order = getOrderWithStoreAccess(orderId, store);

        if (order.getStatus() != Order.OrderStatus.CREATED)
            throw new ApiException("Order must be in CREATED status to be collected",  HttpStatus.BAD_REQUEST);

        order.setStatus(Order.OrderStatus.COLLECTED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        var store = (Store) securityService.getCurrentUser(UserRole.STORE);
        var order = getOrderWithStoreAccess(orderId, store);

        if (order.getStatus() != Order.OrderStatus.CREATED)
            throw new ApiException("Cannot cancel order in current status: " + order.getStatus(), HttpStatus.BAD_REQUEST);

        order.setStatus(Order.OrderStatus.REFUND_PROCESSING);
        order.setCompletedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        var refundMessage = new RefundMessage(
                order.getId(),
                order.getCustomerEmail(),
                false
        );
        jmsService.sendRefundRequest(refundMessage);
        log.info("Sent refund request for order ID: {}", order.getId());
        
        return order;
    }

    private Order getOrderWithStoreAccess(Long orderId, Store store) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (!order.getStore().getId().equals(store.getId()))
            throw new ApiException("Order not found", HttpStatus.NOT_FOUND);

        return order;
    }
}
