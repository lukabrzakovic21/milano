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
    }

//    private boolean checkIfItemWithTypeExist(ItemType type) {
//        var result = itemRepository.findByType(type);
//        return result.isPresent();
//    }

    public void validateParams(Integer limit, Integer offset, String sortBy, String sortDirection, String type) {

        if(limit<0 || offset<0) {
            throw new ItemBadRequest("Limit and offset cannot be lower than 0.");
        }

        if(!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)) {
            throw new ItemBadRequest("Wrong sort direction");
        }

        if(!"price".equalsIgnoreCase(sortBy) && !"numberLeft".equalsIgnoreCase(sortBy) && !"id".equalsIgnoreCase(sortBy)) {
            throw new ItemBadRequest("Wrong sort field");

        }

        if(type.isEmpty()) {
            return;
        }
        if(!ItemType.check(type)) {
            throw new ItemBadRequest("Wrong type for filtering");
        }

    }
}
