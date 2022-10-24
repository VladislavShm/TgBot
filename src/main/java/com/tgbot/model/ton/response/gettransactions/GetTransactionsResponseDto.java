package com.tgbot.model.ton.response.gettransactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class GetTransactionsResponseDto {
    private Long utime;
    @JsonProperty("in_msg")
    private InMsg inMsg;
    @JsonProperty("out_msgs")
    private List<OutMsg> outMsgs;

    @Data
    public static class InMsg {
        private String source;
        private String destination;
        private BigInteger value;
    }

    @Data
    public static class OutMsg {
        private String source;
        private String destination;
        private BigInteger value;
    }
}
