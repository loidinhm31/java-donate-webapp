package com.donation.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaypalBreakDown {

    @JsonProperty("gross_amount")
    private AmountResponse grossAmount;

    @JsonProperty("paypal_fee")
    private AmountResponse paypalFee;

    @JsonProperty("net_amount")
    private AmountResponse netAmount;
}
