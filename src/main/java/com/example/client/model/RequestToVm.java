package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestToVm {
    @JsonProperty
    private String vmId;
    @JsonProperty
    private TypeOfCommand typeOfCommand;
    @JsonProperty
    private ProxyModel proxyModel;
    @JsonProperty
    private String url;
    @JsonProperty
    private int quantity;

}
