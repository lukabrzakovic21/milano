package com.master.milano.manipulator;

import com.master.milano.common.dto.ItemDTO;
import com.master.milano.common.model.Item;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Component
public class ItemManipulator {

    public static int DEFAULT_ITEM_NUMBER = 10;

    public Item dtoToModel(ItemDTO itemDTO) {

        return Item.builder()
                .publicId(UUID.randomUUID())
                .createdAt(Timestamp.from(Instant.now()))
                .name(itemDTO.getName())
                .numberLeft(Objects.nonNull(itemDTO.getNumberLeft()) ? itemDTO.getNumberLeft() : DEFAULT_ITEM_NUMBER)
                .price(itemDTO.getPrice())
                .type(itemDTO.getType())
                .purchaseHistories(new HashSet<>())
                .build();
    }

    public ItemDTO modelToDTO(Item item) {

        return ItemDTO.builder()
                .name(item.getName())
                .numberLeft(item.getNumberLeft())
                .price(item.getPrice())
                .type(item.getType())
                .build();
    }

}
