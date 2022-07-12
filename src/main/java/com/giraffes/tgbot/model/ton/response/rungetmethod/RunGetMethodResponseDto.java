package com.giraffes.tgbot.model.ton.response.rungetmethod;

import lombok.Data;

import java.util.List;

@Data
public class RunGetMethodResponseDto {
    private List<List<Object>> stack;
}
