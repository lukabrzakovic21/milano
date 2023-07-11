package com.master.milano.repository;

import com.master.milano.common.model.Item;
import com.master.milano.common.util.ItemType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<Item, Integer>, CrudRepository<Item, Integer> {

    Optional<Item> findByType(ItemType type);
    Optional<Item> findByPublicId(UUID publicId);
}
