package com.master.milano.repository;

import com.master.milano.common.model.PurchaseHistory;
import org.springframework.data.repository.CrudRepository;

public interface PurchaseHistoryRepository extends CrudRepository<PurchaseHistory, Integer> {

    Iterable<PurchaseHistory> getAllByUserId(String userId);
}
