package com.delivery.service.jms;

import com.delivery.dto.message.RefundMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @Profile("bank")
@RequiredArgsConstructor
@Slf4j
public class BankNodeJmsListener {
    private final JmsTemplate jmsTemplate;

    @Value("${jms.queue.refund.response}")
    private String refundResponseQueue;

    @JmsListener(destination = "${jms.queue.refund}")
    @Transactional
    public void processRefundRequest(RefundMessage refundMessage) {
        log.info("Received refund request for order ID: {} and customer: {}",
                refundMessage.getOrderId(), refundMessage.getCustomerEmail());

        try {
            // Simulate bank processing
            Thread.sleep(10000);

            refundMessage.setRefundProcessed(true);
            log.info("Refund processed successfully for order ID: {}", refundMessage.getOrderId());
        } catch (Exception e) {
            refundMessage.setRefundProcessed(false);
            log.error("Error processing refund for order ID: {}", refundMessage.getOrderId(), e);
        }

        jmsTemplate.convertAndSend(refundResponseQueue, refundMessage);
    }
}
