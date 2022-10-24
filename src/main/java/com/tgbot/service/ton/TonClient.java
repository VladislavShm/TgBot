package com.tgbot.service.ton;

import com.tgbot.model.ton.response.rungetmethod.RunGetMethodResponse;
import com.tgbot.model.ton.response.gettransactions.GetTransactionsResponse;
import com.tgbot.model.ton.response.getwalletinfo.GetWalletInfoResponse;
import com.tgbot.model.ton.request.RunGetMethodRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Component
public class TonClient {
    @Autowired
    @Qualifier("tonRestTemplate")
    private RestTemplate restTemplate;

    public GetTransactionsResponse getTransactions(String address) {
        return call(() -> restTemplate.getForObject(String.format("/getTransactions?address=%s&archival=true", address), GetTransactionsResponse.class));
    }

    public GetWalletInfoResponse getWalletInfo(String address) {
        return call(() -> restTemplate.getForObject(String.format("/getWalletInformation?address=%s&archival=true", address), GetWalletInfoResponse.class));
    }

    public RunGetMethodResponse runGetMethod(RunGetMethodRequest request) {
        return call(() -> restTemplate.postForObject("/runGetMethod", request, RunGetMethodResponse.class));
    }

    @SneakyThrows
    private <R> R call(Supplier<R> method) {
        Exception lastError = null;
        int attempts = 0;
        while (attempts < 15) {
            attempts++;
            try {
                return method.get();
            } catch (Exception e) {
                Thread.sleep(1000);
                lastError = e;
            }
        }

        throw new RuntimeException(lastError);
    }
}
