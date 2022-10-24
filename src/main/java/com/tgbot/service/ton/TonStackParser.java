package com.tgbot.service.ton;

import com.tgbot.model.ton.Address;
import com.tgbot.model.ton.BitString;
import com.tgbot.model.ton.Cell;
import com.tgbot.model.ton.contract.NftSaleContractType;
import com.tgbot.model.ton.response.GetCollectionDataResult;
import com.tgbot.model.ton.response.GetNftAddressByIndexResult;
import com.tgbot.model.ton.response.GetNftDataResult;
import com.tgbot.model.ton.response.GetSaleDataResult;
import com.tgbot.utils.TonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TonStackParser {
    public GetNftDataResult parseNftData(List<List<Object>> stack) {
        GetNftDataResult result = new GetNftDataResult();
        for (int i = 0; i < stack.size(); i++) {
            List<Object> list = stack.get(i);
            Object value = parseStack(list);
            if (value != null) {
                if (i == 0) {
                    result.setInitialized(Boolean.TRUE.equals(value));
                } else if (i == 2) {
                    result.setCollectionAddress(parseAddress((Cell) value));
                } else if (i == 3) {
                    result.setOwner(parseAddress((Cell) value));
                }
            }
        }

        return result;
    }

    public GetSaleDataResult parseSaleData(List<List<Object>> stack) {
        NftSaleContractType nftSaleContractType = stack.size() == 11 ? NftSaleContractType.GETGEMS : NftSaleContractType.DISINTAR;

        int fullPriceIndex = nftSaleContractType == NftSaleContractType.GETGEMS ? 6 : 3;
        int nftOwnerIndex = nftSaleContractType == NftSaleContractType.GETGEMS ? 5 : 2;

        GetSaleDataResult result = new GetSaleDataResult(true);
        for (int i = 0; i < stack.size(); i++) {
            List<Object> list = stack.get(i);
            Object value = parseStack(list);
            if (value != null) {
                if (i == fullPriceIndex) {
                    result.setFullPrice(BigInteger.valueOf((Long) value));
                } else if (i == nftOwnerIndex) {
                    result.setOwner(parseAddress((Cell) value));
                }
            }
        }

        return result;
    }

    public GetCollectionDataResult parseCollectionData(List<List<Object>> stack) {
        GetCollectionDataResult result = new GetCollectionDataResult();
        for (int i = 0; i < stack.size(); i++) {
            List<Object> list = stack.get(i);
            Object value = parseStack(list);
            if (value != null) {
                if (i == 0) {
                    result.setNextNftIndex((Long) value);
                }
            }
        }

        return result;
    }

    public GetNftAddressByIndexResult parseNftAddressByIndex(List<List<Object>> stack) {
        GetNftAddressByIndexResult result = new GetNftAddressByIndexResult();
        for (int i = 0; i < stack.size(); i++) {
            List<Object> list = stack.get(i);
            Object value = parseStack(list);
            if (value != null) {
                if (i == 0) {
                    result.setAddress(parseAddress((Cell) value));
                }
            }
        }

        return result;
    }

    private Address parseAddress(Cell cell) {
        BigInteger n = readIntFromBitString(cell.getBits(), 3, 8);
        if (n.compareTo(BigInteger.valueOf(127)) > 0) {
            n = n.subtract(BigInteger.valueOf(256));
        }
        BigInteger hashPart = readIntFromBitString(cell.getBits(), 3 + 8, 256);
        if ((n + ":" + hashPart).equals("0:0")) return null;
        String s = n + ":" + StringUtils.leftPad(hashPart.toString(16), 64, '0');
        return new Address(s);
    }

    private BigInteger readIntFromBitString(BitString bs, int cursor, int bits) {
        BigInteger n = BigInteger.ZERO;
        for (int i = 0; i < bits; i++) {
            n = n.multiply(BigInteger.valueOf(2));
            n = n.add(bs.get(cursor + i) ? BigInteger.ONE : BigInteger.ZERO);
        }
        return n;
    }

    private Object parseStack(List<Object> stack) {
        String type = (String) stack.get(0);
        Object value = stack.get(1);

        switch (type) {
            case "num":
                return Long.parseLong(((String) value).replace("0x", ""), 16);
//            case 'list':
            case "tuple":
                return parseObject((Map<String, Object>) value);
            case "cell":
                Map map = (Map) value;
                byte[] bytes = Base64.getDecoder().decode((String) map.get("bytes"));

                int[] ints = new int[bytes.length];
                for (int i = 0; i < bytes.length; i++) {
                    ints[i] = bytes[i] < 0 ? 256 + bytes[i] : bytes[i];
                }

                return TonUtils.deserializeBoc(ints).get(0);
            default:
                log.info("Unknown type {}", type);
        }

        return null;
    }

    private Object parseObject(Map<String, Object> value) {
        String typeName = (String) value.get("@type");
        switch (typeName) {
//            case "tvm.list":
//            case "tvm.tuple":
//                return value.elements.map(HttpProviderUtils.parseObject);
//            case "tvm.stackEntryTuple":
//                return HttpProviderUtils.parseObject(value.tuple);
//            case "tvm.stackEntryNumber":
//                return HttpProviderUtils.parseObject(value.number);
            case "tvm.numberDecimal":
                return new BigInteger((String) value.get("number"));
            default:
                log.info("Unknown type {}", typeName);
        }

        return null;
    }
}
