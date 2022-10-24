package com.tgbot.model.ton.response;

import com.tgbot.model.ton.Address;
import lombok.Data;

@Data
public class GetNftAddressByIndexResult {
    private Address address;
}
