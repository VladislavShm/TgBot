package com.giraffes.tgbot.processor.utils;

import com.giraffes.tgbot.model.ton.BocHeader;
import com.giraffes.tgbot.utils.TonUtils;
import org.junit.jupiter.api.Test;

import static com.giraffes.tgbot.utils.TonUtils.parseBocHeader;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TonUtilsTest {
    @Test
    public void hexToBytesTest() {
        assertEquals(
                "48656c6c6f20776f726c6421",
                TonUtils.bytesToHex(new int[]{72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33})
        );
    }

    @Test
    public void bytesToHexTest() {
        assertArrayEquals(
                new int[]{72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33},
                TonUtils.hexToBytes("48656c6c6f20776f726c6421")
        );
    }

    @Test
    public void uintToHexTest() {
        assertEquals(
                "01",
                TonUtils.uintToHex("1")
        );
        assertEquals(
                "00",
                TonUtils.uintToHex("0")
        );
    }

    @Test
    public void parseBocHeaderTest() {
        int[] bytes = TonUtils.hexToBytes("B5EE9C7241020F01000262000114FF00F4A413F4BCF2C80B0102012002030201480405036AF230DB3C5335A127A904F82327A128A90401BC5135A0F823B913B0F29EF800725210BE945387F0078E855386DB3CA4E2F82302DB3C0B0C0D0202CD06070121A0D0C9B67813F488DE0411F488DE0410130B048FD6D9E05E8698198FD201829846382C74E2F841999E98F9841083239BA395D497803F018B841083AB735BBED9E702984E382D9C74688462F863841083AB735BBED9E70156BA4E09040B0A0A080269F10FD22184093886D9E7C12C1083239BA39384008646582A803678B2801FD010A65B5658F89659FE4B9FD803FC1083239BA396D9E40E0A04F08E8D108C5F0C708210756E6B77DB3CE00AD31F308210706C7567831EB15210BA8F48305324A126A904F82326A127A904BEF27109FA4430A619F833D078D721D70B3F5260A11BBE8E923036F82370708210737562732759DB3C5077DE106910581047103645135042DB3CE0395F076C2232821064737472BA0A0A0D09011A8E897F821064737472DB3CE0300A006821B39982100400000072FB02DE70F8276F118010C8CB055005CF1621FA0214F40013CB6912CB1F830602948100A032DEC901FB000030ED44D0FA40FA40FA00D31FD31FD31FD31FD31FD307D31F30018021FA443020813A98DB3C01A619F833D078D721D70B3FA070F8258210706C7567228018C8CB055007CF165004FA0215CB6A12CB1F13CB3F01FA02CB00C973FB000E0040C8500ACF165008CF165006FA0214CB1F12CB1FCB1FCB1FCB1FCB07CB1FC9ED54005801A615F833D020D70B078100D1BA95810088D721DED307218100DDBA028100DEBA12B1F2E047D33F30A8AB0FE5855AB4");

        BocHeader result = parseBocHeader(bytes);
        assertEquals(0, result.getAbsent_num());
        assertArrayEquals(new int[]{1, 20, 255, 0, 244, 164, 19, 244, 188, 242, 200, 11, 1, 2, 1, 32, 2, 3, 2, 1, 72, 4, 5, 3, 106, 242, 48, 219, 60, 83, 53, 161, 39, 169, 4, 248, 35, 39, 161, 40, 169, 4, 1, 188, 81, 53, 160, 248, 35, 185, 19, 176, 242, 158, 248, 0, 114, 82, 16, 190, 148, 83, 135, 240, 7, 142, 133, 83, 134, 219, 60, 164, 226, 248, 35, 2, 219, 60, 11, 12, 13, 2, 2, 205, 6, 7, 1, 33, 160, 208, 201, 182, 120, 19, 244, 136, 222, 4, 17, 244, 136, 222, 4, 16, 19, 11, 4, 143, 214, 217, 224, 94, 134, 152, 25, 143, 210, 1, 130, 152, 70, 56, 44, 116, 226, 248, 65, 153, 158, 152, 249, 132, 16, 131, 35, 155, 163, 149, 212, 151, 128, 63, 1, 139, 132, 16, 131, 171, 115, 91, 190, 217, 231, 2, 152, 78, 56, 45, 156, 116, 104, 132, 98, 248, 99, 132, 16, 131, 171, 115, 91, 190, 217, 231, 1, 86, 186, 78, 9, 4, 11, 10, 10, 8, 2, 105, 241, 15, 210, 33, 132, 9, 56, 134, 217, 231, 193, 44, 16, 131, 35, 155, 163, 147, 132, 0, 134, 70, 88, 42, 128, 54, 120, 178, 128, 31, 208, 16, 166, 91, 86, 88, 248, 150, 89, 254, 75, 159, 216, 3, 252, 16, 131, 35, 155, 163, 150, 217, 228, 14, 10, 4, 240, 142, 141, 16, 140, 95, 12, 112, 130, 16, 117, 110, 107, 119, 219, 60, 224, 10, 211, 31, 48, 130, 16, 112, 108, 117, 103, 131, 30, 177, 82, 16, 186, 143, 72, 48, 83, 36, 161, 38, 169, 4, 248, 35, 38, 161, 39, 169, 4, 190, 242, 113, 9, 250, 68, 48, 166, 25, 248, 51, 208, 120, 215, 33, 215, 11, 63, 82, 96, 161, 27, 190, 142, 146, 48, 54, 248, 35, 112, 112, 130, 16, 115, 117, 98, 115, 39, 89, 219, 60, 80, 119, 222, 16, 105, 16, 88, 16, 71, 16, 54, 69, 19, 80, 66, 219, 60, 224, 57, 95, 7, 108, 34, 50, 130, 16, 100, 115, 116, 114, 186, 10, 10, 13, 9, 1, 26, 142, 137, 127, 130, 16, 100, 115, 116, 114, 219, 60, 224, 48, 10, 0, 104, 33, 179, 153, 130, 16, 4, 0, 0, 0, 114, 251, 2, 222, 112, 248, 39, 111, 17, 128, 16, 200, 203, 5, 80, 5, 207, 22, 33, 250, 2, 20, 244, 0, 19, 203, 105, 18, 203, 31, 131, 6, 2, 148, 129, 0, 160, 50, 222, 201, 1, 251, 0, 0, 48, 237, 68, 208, 250, 64, 250, 64, 250, 0, 211, 31, 211, 31, 211, 31, 211, 31, 211, 31, 211, 7, 211, 31, 48, 1, 128, 33, 250, 68, 48, 32, 129, 58, 152, 219, 60, 1, 166, 25, 248, 51, 208, 120, 215, 33, 215, 11, 63, 160, 112, 248, 37, 130, 16, 112, 108, 117, 103, 34, 128, 24, 200, 203, 5, 80, 7, 207, 22, 80, 4, 250, 2, 21, 203, 106, 18, 203, 31, 19, 203, 63, 1, 250, 2, 203, 0, 201, 115, 251, 0, 14, 0, 64, 200, 80, 10, 207, 22, 80, 8, 207, 22, 80, 6, 250, 2, 20, 203, 31, 18, 203, 31, 203, 31, 203, 31, 203, 31, 203, 7, 203, 31, 201, 237, 84, 0, 88, 1, 166, 21, 248, 51, 208, 32, 215, 11, 7, 129, 0, 209, 186, 149, 129, 0, 136, 215, 33, 222, 211, 7, 33, 129, 0, 221, 186, 2, 129, 0, 222, 186, 18, 177, 242, 224, 71, 211, 63, 48, 168, 171, 15}, result.getCells_data());
        assertEquals(0, result.getFlags());
        assertEquals(64, result.getHash_crc32());
        assertEquals(0, result.getHas_cache_bits());
        assertEquals(0, result.getHas_idx());
        assertArrayEquals(new int[]{0}, result.getRoot_list());
        assertEquals(15, result.getCells_num());
        assertEquals(2, result.getOff_bytes());
        assertEquals(1, result.getSize_bytes());
        assertEquals(1, result.getRoots_num());
        assertEquals(610, result.getTot_cells_size());
        assertNull(result.getIndex());
    }
}
