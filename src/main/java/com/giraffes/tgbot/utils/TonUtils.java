package com.giraffes.tgbot.utils;

import com.giraffes.tgbot.model.ton.BocHeader;
import com.giraffes.tgbot.model.ton.Cell;
import com.giraffes.tgbot.model.ton.CellDeserializationResult;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TonUtils {
    private static final int[] REACH_BOC_MAGIC_PREFIX = hexToBytes("B5EE9C72");
    private static final int[] LEAN_BOC_MAGIC_PREFIX = hexToBytes("68ff65f3");
    private static final int[] LEAN_BOC_MAGIC_PREFIX_CRC = hexToBytes("acc3a728");

    public static int[] hexToBytes(String hex) {
        hex = hex.toLowerCase();
        int length2 = hex.length();
        if (length2 % 2 != 0) {
            throw new RuntimeException("hex string must have length a multiple of 2");
        }

        Matcher matcher = Pattern.compile(".{1,2}").matcher(hex);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }

        List<Integer> list = new ArrayList<>();
        for (String b : matches) {
            list.add(Integer.valueOf(b, 16));
        }

        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }

        return result;
    }

    public static String bytesToHex(int[] bytes) {
        return Arrays.stream(bytes)
                .boxed()
                .map(uint -> Integer.toString(uint, 16))
                .reduce("", (acc, uint) -> acc + uintToHex(uint));
    }

    public static String uintToHex(String uint) {
        String hex = "0" + uint;
        return hex.substring(Math.toIntExact(hex.length() - Math.round(Math.floor(hex.length() >> 1) * 2)));
    }

    public static BocHeader parseBocHeader(int[] serializedBoc) {
        // snake_case is used to match TON docs
        if (serializedBoc.length < 4 + 1)
            throw new RuntimeException("Not enough bytes for magic prefix");

        int[] inputData = serializedBoc; // Save copy for crc32
        int[] prefix = Arrays.copyOf(serializedBoc, 4);
        serializedBoc = Arrays.copyOfRange(serializedBoc, 4, serializedBoc.length);
        Integer has_idx = null, hash_crc32 = null, has_cache_bits = null, flags = null, size_bytes = null;
        if (Arrays.equals(prefix, REACH_BOC_MAGIC_PREFIX)) {
            int flags_byte = serializedBoc[0];
            has_idx = flags_byte & 128;
            hash_crc32 = flags_byte & 64;
            has_cache_bits = flags_byte & 32;
            flags = (flags_byte & 16) * 2 + (flags_byte & 8);
            size_bytes = flags_byte % 8;
        }

        if (Arrays.equals(prefix, LEAN_BOC_MAGIC_PREFIX)) {
            has_idx = 1;
            hash_crc32 = 0;
            has_cache_bits = 0;
            flags = 0;
            size_bytes = serializedBoc[0];
        }

        if (Arrays.equals(prefix, LEAN_BOC_MAGIC_PREFIX_CRC)) {
            has_idx = 1;
            hash_crc32 = 1;
            has_cache_bits = 0;
            flags = 0;
            size_bytes = serializedBoc[0];
        }

        if (size_bytes == null) {
            throw new RuntimeException("size_bytes is not initialized");
        }

        serializedBoc = Arrays.copyOfRange(serializedBoc, 1, serializedBoc.length);
        if (serializedBoc.length < 1 + 5 * size_bytes)
            throw new RuntimeException("Not enough bytes for encoding cells counters");

        int offset_bytes = serializedBoc[0];
        serializedBoc = Arrays.copyOfRange(serializedBoc, 1, serializedBoc.length);
        int cells_num = readNBytesUIntFromArray(size_bytes, serializedBoc);
        serializedBoc = Arrays.copyOfRange(serializedBoc, size_bytes, serializedBoc.length);
        int roots_num = readNBytesUIntFromArray(size_bytes, serializedBoc);
        serializedBoc = Arrays.copyOfRange(serializedBoc, size_bytes, serializedBoc.length);
        int absent_num = readNBytesUIntFromArray(size_bytes, serializedBoc);
        serializedBoc = Arrays.copyOfRange(serializedBoc, size_bytes, serializedBoc.length);
        int tot_cells_size = readNBytesUIntFromArray(offset_bytes, serializedBoc);
        serializedBoc = Arrays.copyOfRange(serializedBoc, offset_bytes, serializedBoc.length);

        if (serializedBoc.length < roots_num * size_bytes)
            throw new RuntimeException("Not enough bytes for encoding root cells hashes");

        int[] root_list = new int[roots_num];
        for (int c = 0; c < roots_num; c++) {
            root_list[c] = readNBytesUIntFromArray(size_bytes, serializedBoc);
            serializedBoc = Arrays.copyOfRange(serializedBoc, size_bytes, serializedBoc.length);
        }

        int[] index = null;
        if (has_idx != 0) {
            index = new int[cells_num];
            if (serializedBoc.length < offset_bytes * cells_num)
                throw new RuntimeException("Not enough bytes for index encoding");

            for (int c = 0; c < cells_num; c++) {
                index[c] = readNBytesUIntFromArray(offset_bytes, serializedBoc);
                serializedBoc = Arrays.copyOfRange(serializedBoc, offset_bytes, serializedBoc.length);
            }
        }

        if (serializedBoc.length < tot_cells_size)
            throw new RuntimeException("Not enough bytes for cells data");

        int[] cells_data = Arrays.copyOfRange(serializedBoc, 0, tot_cells_size);
        serializedBoc = Arrays.copyOfRange(serializedBoc, tot_cells_size, serializedBoc.length);
        if (hash_crc32 != 0) {
            if (serializedBoc.length < 4)
                throw new RuntimeException("Not enough bytes for crc32c hashsum");

//            int length = inputData.length;
//            if (!Arrays.equals(createCrc32c(Arrays.copyOf(inputData, length - 4)), Arrays.copyOf(serializedBoc, 4)))
//                throw new RuntimeException("Crc32c hashsum mismatch");

            serializedBoc = Arrays.copyOfRange(serializedBoc, 4, serializedBoc.length);
        }

        if (serializedBoc.length > 0)
            throw new RuntimeException("Too much bytes in BoC serialization");

        return new BocHeader(
                has_idx, hash_crc32, has_cache_bits, flags, size_bytes,
                offset_bytes, cells_num, roots_num, absent_num, tot_cells_size,
                root_list, index, cells_data
        );
    }

    public List<Cell> deserializeBoc(int[] serializedBoc) {
        BocHeader header = parseBocHeader(serializedBoc);
        int[] cells_data = header.getCells_data();
        List<Cell> cells_array = new ArrayList<>();
        for (int ci = 0; ci < header.getCells_num(); ci++) {
            CellDeserializationResult dd = deserializeCellData(cells_data, header.getSize_bytes());
            cells_data = dd.getResidue();
            cells_array.add(dd.getCell());
        }

        for (int ci = header.getCells_num() - 1; ci >= 0; ci--) {
            Cell c = cells_array.get(ci);
            for (int ri = 0; ri < c.getRefs().size(); ri++) {
                int r = c.getRefsAsInt().get(ri);
                if (r < ci) {
                    throw new RuntimeException("Topological order is broken");
                }
                c.getRefs().add(cells_array.get(r));
            }
        }

        List<Cell> root_cells = new ArrayList<>();
        for (int ri : header.getRoot_list()) {
            root_cells.add(cells_array.get(ri));
        }
        return root_cells;
    }

    public CellDeserializationResult deserializeCellData(int[] cellData, int referenceIndexSize) {
        if (cellData.length < 2)
            throw new RuntimeException("Not enough bytes to encode cell descriptors");
        int d1 = cellData[0], d2 = cellData[1];
        cellData = Arrays.copyOfRange(cellData, 2, cellData.length);
        int isExotic = d1 & 8;
        int refNum = d1 % 8;
        int dataBytesize = Double.valueOf(Math.ceil((double) d2 / 2)).intValue();
        boolean fullfilledBytes = d2 % 2 == 0;
        Cell cell = new Cell();
        cell.setIsExotic(isExotic);
        if (cellData.length < dataBytesize + referenceIndexSize * refNum)
            throw new RuntimeException("Not enough bytes to encode cell data");

        cell.getBits().setTopUppedArray(Arrays.copyOfRange(cellData, 0, dataBytesize), fullfilledBytes);
        cellData = Arrays.copyOfRange(cellData, dataBytesize, cellData.length);
        for (int r = 0; r < refNum; r++) {
            cell.getRefsAsInt().add(readNBytesUIntFromArray(referenceIndexSize, cellData));
            cellData = Arrays.copyOfRange(cellData, referenceIndexSize, cellData.length);
        }
        return new CellDeserializationResult(cell, cellData);

    }

    private int readNBytesUIntFromArray(int n, int[] ui8array) {
        int res = 0;
        for (int c = 0; c < n; c++) {
            res *= 256;
            res += ui8array[c];
        }
        return res;
    }
}
