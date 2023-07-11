package com.master.milano.validator;

import com.master.milano.common.dto.ItemDTO;
import com.master.milano.common.util.ItemType;
import com.master.milano.exception.item.ItemBadRequest;
import com.master.milano.repository.ItemRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class ItemValidator {

    private final ItemRepository itemRepository;

    public ItemValidator(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void validateNewItem(ItemDTO itemDTO) {

        if(Objects.isNull(itemDTO) || Objects.isNull(itemDTO.getName()) || Objects.isNull(itemDTO.getPrice())
                 || Objects.isNull(itemDTO.getType())) {
            throw new ItemBadRequest("Fields name, price, type are mandatory");
        }
        if(itemDTO.getName().isBlank() || itemDTO.getType().name().isBlank()) {
            throw new ItemBadRequest("Fields name, price, type are mandatory");
        }

        if(checkIfItemWithTypeExist(itemDTO.getType())) {
            throw new ItemBadRequest("Item with this type already exists.");
        }
    }

    private boolean checkIfItemWithTypeExist(ItemType type) {
        var result = itemRepository.findByType(type);
        return result.isPresent();
    }
}
