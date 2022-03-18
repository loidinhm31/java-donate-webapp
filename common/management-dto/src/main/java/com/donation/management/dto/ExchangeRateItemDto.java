package com.donation.management.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateItemDto {
    private String type;

    @JsonProperty("bantienmat")
    private String buyCash;

    @JsonProperty("banck")
    private String buyTransfer;
}
