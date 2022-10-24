package com.tgbot.model.ton.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RunGetMethodRequest {
    private final String address;
    private final String method;
    private String[][] stack = new String[0][0];
}
