package com.master.milano.common.util;

import com.master.milano.common.model.Item;

public enum ItemType {
    SPECIAL_STICKER,
    REGULAR_STICKER,
    STICKER_ALBUM,
    STICKER_SMALL_PACK,
    STICKER_REGULAR_PACK,
    STICKER_BIG_PACK;


    public static ItemType fromString(String type) {
        return valueOf(type.toUpperCase());
    }

    public static boolean check(String type){
        if(type == null || type.isEmpty()) {
            return false;
        }

        for (ItemType existingType : ItemType.values()) {
            if(existingType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }

        return false;
    }
}
