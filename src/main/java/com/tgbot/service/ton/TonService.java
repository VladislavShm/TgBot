package com.tgbot.service.ton;

import com.tgbot.model.ton.request.RunGetMethodRequest;
import com.tgbot.model.ton.response.GetCollectionDataResult;
import com.tgbot.model.ton.response.GetNftAddressByIndexResult;
import com.tgbot.model.ton.response.GetNftDataResult;
import com.tgbot.model.ton.response.GetSaleDataResult;
import com.tgbot.model.ton.response.gettransactions.GetTransactionsResponseDto;
import com.tgbot.model.ton.response.getwalletinfo.GetWalletInfoDto;
import com.tgbot.model.ton.response.rungetmethod.RunGetMethodResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TonService {
    private final TonStackParser tonStackParser;
    private final TonClient tonClient;

    public List<GetTransactionsResponseDto> getTransactions(String address) {
        return tonClient.getTransactions(address).getResult();
    }

    public GetWalletInfoDto getWalletInfo(String address) {
        return tonClient.getWalletInfo(address).getResult();
    }

    public GetNftDataResult getNftData(String nftAddress) {
        RunGetMethodResponse response = tonClient.runGetMethod(new RunGetMethodRequest(nftAddress, "get_nft_data"));
        return tonStackParser.parseNftData(response.getResult().getStack());
    }

    public GetSaleDataResult getSaleData(String saleAddress) {
        try {
            RunGetMethodResponse response = tonClient.runGetMethod(new RunGetMethodRequest(saleAddress, "get_sale_data"));
            return tonStackParser.parseSaleData(response.getResult().getStack());
        } catch (Exception e) {
            return new GetSaleDataResult(false);
        }
    }

    public GetCollectionDataResult getCollectionData(String collectionAddress) {
        RunGetMethodResponse response = tonClient.runGetMethod(new RunGetMethodRequest(collectionAddress, "get_collection_data"));
        return tonStackParser.parseCollectionData(response.getResult().getStack());
    }

    public GetNftAddressByIndexResult getNftAddressByIndex(String collectionAddress, int index) {
        String[][] stack = new String[1][2];
        stack[0][0] = "num";
        stack[0][1] = String.valueOf(index);

        RunGetMethodResponse response = tonClient.runGetMethod(new RunGetMethodRequest(collectionAddress, "get_nft_address_by_index", stack));
        return tonStackParser.parseNftAddressByIndex(response.getResult().getStack());
    }

}
