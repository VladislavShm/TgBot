package com.giraffes.tgbot.model.ton.response;

import com.giraffes.tgbot.model.ton.Address;
import lombok.Data;

@Data
public class GetNftAddressByIndexResult {
    private Address address;
}
