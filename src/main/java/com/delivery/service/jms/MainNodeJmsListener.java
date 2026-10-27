package com.delivery.service.jms;

import com.delivery.dto.message.CourierAssignmentMessage;
import com.delivery.dto.message.RefundMessage;
import com.delivery.model.Courier;
import com.delivery.model.Order;
import com.delivery.repository.CourierRepository;
import com.delivery.repository.OrderRepository;
import com.delivery.service.CourierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component @Profile("main")
@RequiredArgsConstructor
@Slf4j
public class MainNodeJmsListener {
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;

    @JmsListener(destination = "${jms.queue.refund.response}")
    @Transactional
    public void receiveRefundResponse(RefundMessage refundMessage) {
        log.info("Received refund response for order ID: {}, refund processed: {}", 
                refundMessage.getOrderId(), refundMessage.isRefundProcessed());
        
        if (refundMessage.isRefundProcessed()) {
            var order = orderRepository.findById(refundMessage.getOrderId()).orElse(null);
            if (order != null && order.getStatus() == Order.OrderStatus.REFUND_PROCESSING) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
                log.info("Order {} status updated to CANCELLED", order.getId());
            } else {
                log.warn("Order not found or not in REFUND_PROCESSING status: {}", refundMessage.getOrderId());
            }
        } else {
            log.error("Refund processing failed for order ID: {}", refundMessage.getOrderId());
        }
    }

    @JmsListener(destination = "${jms.queue.courier.assignment.response}")
    @Transactional(propagation = Propagation.MANDATORY)
    public void receiveCourierAssignmentResponse(CourierAssignmentMessage assignmentMessage) {
        log.info("Received courier assignment response for courier ID: {}, assigned order: {}", 
                assignmentMessage.getCourierId(), assignmentMessage.getAssignedOrderId());
        
        if (assignmentMessage.getAssignedOrderId() != null) {
            var order = orderRepository.findById(assignmentMessage.getAssignedOrderId()).orElse(null);
            if (order == null || order.getStatus() != Order.OrderStatus.COLLECTED) {
                log.warn("Order not found or not in COLLECTED status: {}", assignmentMessage.getAssignedOrderId());
                return;
            }

            var courier = courierRepository.findById(assignmentMessage.getCourierId()).orElse(null);
            if (courier == null) {
                log.warn("Courier not found with ID: {}", assignmentMessage.getCourierId());
                return;
            }

            order.setStatus(Order.OrderStatus.IN_DELIVERY);
            order.setCourier(courier);
            orderRepository.save(order);
            log.info("Order {} updated, new status is IN_DELIVERY", order.getId());
        } else {
            log.warn("No order assigned to courier ID: {}", assignmentMessage.getCourierId());
        }
    }
}
