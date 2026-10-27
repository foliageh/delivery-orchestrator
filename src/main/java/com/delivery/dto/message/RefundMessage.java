package com.delivery.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundMessage implements Serializable {
    private Long orderId;
    private String customerEmail;
    private boolean refundProcessed;
}
