package com.giraffes.tgbot.model.ton.response;

import com.giraffes.tgbot.model.ton.Address;
import lombok.Data;

@Data
public class GetNftDataResult {
    private boolean initialized;
    private Address collectionAddress;
    private Address owner;
}
