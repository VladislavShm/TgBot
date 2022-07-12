package com.giraffes.tgbot.model.ton.response;

import lombok.Data;

@Data
public class TonBaseResponse<R> {
    private boolean ok;
    private R result;
}
