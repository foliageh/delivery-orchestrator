package com.delivery.service.jms;

import com.delivery.dto.message.CourierAssignmentMessage;
import com.delivery.model.Courier;
import com.delivery.model.Order;
import com.delivery.repository.CourierRepository;
import com.delivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component @Profile("courier")
@RequiredArgsConstructor
@Slf4j
public class CourierNodeJmsListener {
    private final JmsTemplate jmsTemplate;
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    @Value("${jms.queue.courier.assignment.response}")
    private String courierAssignmentResponseQueue;

    @JmsListener(destination = "${jms.queue.courier.assignment}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processCourierAssignmentRequest(CourierAssignmentMessage assignmentMessage) {
        log.info("Received courier assignment request for courier ID: {}", assignmentMessage.getCourierId());
        
        try {
            var courier = courierRepository.findById(assignmentMessage.getCourierId()).orElse(null);
            if (courier == null || courier.getStatus() != Courier.CourierStatus.READY) {
                log.warn("Courier is not in READY status: {}", assignmentMessage.getCourierId());
                jmsTemplate.convertAndSend(courierAssignmentResponseQueue, assignmentMessage);
                return;
            }
            
            var order = orderRepository.findFirstByStatusOrderByCreatedAtAsc(Order.OrderStatus.COLLECTED).orElse(null);
            if (order == null) {
                log.info("No orders available for assignment to courier: {}", courier.getId());
                jmsTemplate.convertAndSend(courierAssignmentResponseQueue, assignmentMessage);
                return;
            }

            courier.setStatus(Courier.CourierStatus.BUSY);
            courier.setLastAssignment(LocalDateTime.now());
            courierRepository.save(courier);
            
            assignmentMessage.setAssignedOrderId(order.getId());
            log.info("Found order {} to courier {}", order.getId(), courier.getId());
        } catch (Exception e) {
            log.error("Error processing courier assignment for courier ID: {}", assignmentMessage.getCourierId(), e);
        }

        jmsTemplate.convertAndSend(courierAssignmentResponseQueue, assignmentMessage);
    }
}
