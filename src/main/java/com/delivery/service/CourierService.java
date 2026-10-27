package com.delivery.service;

import com.delivery.dto.message.CourierAssignmentMessage;
import com.delivery.exception.ApiException;
import com.delivery.model.Courier;
import com.delivery.model.Order;
import com.delivery.repository.CourierRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.security.SecurityService;
import com.delivery.security.UserRole;
import com.delivery.service.bitrix.BitrixService;
import com.delivery.service.jms.JmsService;
import jakarta.resource.ResourceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierService {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final SecurityService securityService;
    private final JmsService jmsService;
    private final BitrixService bitrixService;

    public Courier getProfile() {
        return (Courier) securityService.getCurrentUser(UserRole.COURIER);
    }

    public Order getOrder(Long orderId) {
        var courier = (Courier) securityService.getCurrentUser(UserRole.COURIER);
        return getOrderWithCourierAccess(orderId, courier);
    }

    public Page<Order> getOrders(int pageNumber, int pageSize) {
        var courier = (Courier) securityService.getCurrentUser(UserRole.COURIER);
        return orderRepository.findByCourier(courier, PageRequest.of(pageNumber, pageSize));
    }

    public Order deliverOrder(Long orderId) {
        var courier = (Courier) securityService.getCurrentUser(UserRole.COURIER);
        if (courier.getStatus() != Courier.CourierStatus.BUSY)
            throw new ApiException("Courier must be in BUSY status to deliver order", HttpStatus.BAD_REQUEST);

        var order = getOrderWithCourierAccess(orderId, courier);
        if (order.getStatus() != Order.OrderStatus.IN_DELIVERY)
            throw new ApiException("Order must be in IN_DELIVERY status to be delivered", HttpStatus.BAD_REQUEST);

        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setCompletedAt(LocalDateTime.now());

        courier.setStatus(Courier.CourierStatus.NOT_READY);
        courierRepository.save(courier);

        return orderRepository.save(order);
    }

    @Transactional
    public Courier makeCourierReady() {
        var courier = (Courier) securityService.getCurrentUser(UserRole.COURIER);
        if (courier.getStatus() == Courier.CourierStatus.BUSY)
            throw new ApiException("Courier is busy", HttpStatus.BAD_REQUEST);

        courier.setStatus(Courier.CourierStatus.READY);
        courier = courierRepository.save(courier);

        var assignmentMessage = new CourierAssignmentMessage(
                courier.getId(),
                courier.getEmail(),
                null
        );
        jmsService.sendCourierAssignmentRequest(assignmentMessage);
        log.info("Sent courier assignment request for courier ID: {}", courier.getId());

        return courier;
    }

    private Order getOrderWithCourierAccess(Long orderId, Courier courier) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (order.getCourier() == null || !order.getCourier().getId().equals(courier.getId()))
            throw new ApiException("Order not found", HttpStatus.NOT_FOUND);

        return order;
    }

    @Profile("courier")
    @Scheduled(fixedDelay = 7*24*60*60*1000, initialDelay = 5000)
    public void checkInactiveCouriers() {
        log.info("Starting scheduled check for inactive couriers");

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Courier> inactiveCouriers = courierRepository.findByLastAssignmentBeforeAndLastAssignmentIsNotNull(oneWeekAgo);
        log.info("Found {} inactive couriers who haven't taken orders in the last week", inactiveCouriers.size());

        for (Courier courier : inactiveCouriers) {
            try {
                String response = bitrixService.createCourierDismissalTask(courier);
                log.info("Sent dismissal task for courier {} to Bitrix: {}", courier.getId(), response);
            } catch (ResourceException e) {
                log.error("Failed to sent dismissal task for courier {} to bitrix: {}", courier.getId(), e.getMessage());
            }
        }
        
        log.info("Completed scheduled check for inactive couriers");
    }
}
