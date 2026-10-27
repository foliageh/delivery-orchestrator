package com.delivery.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourierAssignmentMessage implements Serializable {
    private Long courierId;
    private String courierEmail;
    private Long assignedOrderId;
}
