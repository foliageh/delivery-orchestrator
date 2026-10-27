package com.delivery.service.jms;

import com.delivery.dto.message.CourierAssignmentMessage;
import com.delivery.dto.message.RefundMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JmsService {
    private final JmsTemplate jmsTemplate;

    @Value("${jms.queue.refund}")
    private String refundQueue;

    @Value("${jms.queue.courier.assignment}")
    private String courierAssignmentQueue;

    public void sendRefundRequest(RefundMessage refundMessage) {
        log.info("Sending refund request for order ID: {}", refundMessage.getOrderId());
        jmsTemplate.convertAndSend(refundQueue, refundMessage);
    }

    public void sendCourierAssignmentRequest(CourierAssignmentMessage assignmentMessage) {
        log.info("Sending courier assignment request for courier ID: {}", assignmentMessage.getCourierId());
        jmsTemplate.convertAndSend(courierAssignmentQueue, assignmentMessage);
    }
}
