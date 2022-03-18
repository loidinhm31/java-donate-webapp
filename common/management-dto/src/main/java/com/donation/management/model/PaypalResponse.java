package com.donation.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaypalResponse {
    private String id;

    private String status;

    @JsonProperty("seller_receivable_breakdown")
    private PaypalBreakDown paypalBreakDown;

}
