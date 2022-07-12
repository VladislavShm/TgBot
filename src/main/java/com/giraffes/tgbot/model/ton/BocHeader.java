package com.giraffes.tgbot.model.ton;

import lombok.Data;

@Data
public class BocHeader {
    private final int has_idx;
    private final int hash_crc32;
    private final int has_cache_bits;
    private final int flags;
    private final int size_bytes;
    private final int off_bytes;
    private final int cells_num;
    private final int roots_num;
    private final int absent_num;
    private final int tot_cells_size;
    private final int[] root_list;
    private final int[] index;
    private final int[] cells_data;
}
