package com.master.milano.repository;

import com.master.milano.common.model.Item;
import com.master.milano.common.model.ItemInterest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ItemInterestRepository extends CrudRepository<ItemInterest, Long> {

    Optional<ItemInterest> findByUserIdAndItem(String userId, Item item);


    @Query(
            value = "SELECT ii.* FROM item_interest ii join item i on ii.item_id = i.id  WHERE i.public_id=?1",
            nativeQuery = true)
    Collection<ItemInterest> findAllByPublicId(UUID publicId);
}
