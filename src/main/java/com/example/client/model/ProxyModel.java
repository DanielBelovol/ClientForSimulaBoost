package com.example.client.model;

import lombok.Data;

@Data
public class ProxyModel {
    private String ip;
    private int port;
    private String user;
    private String password;
}
