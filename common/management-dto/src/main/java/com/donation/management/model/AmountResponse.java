package com.donation.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmountResponse {
    @JsonProperty(value = "currency_code")
    private String currencyCode;

    @JsonProperty(value = "value")
    private String value;
}
