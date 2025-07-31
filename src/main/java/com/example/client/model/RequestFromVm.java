package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestFromVm {
    @JsonProperty
    private String vmId;
    @JsonProperty
    private TypeOfCommand typeOfCommand;
    @JsonProperty
    private String url;
}
