package com.master.milano.repository;

import com.master.milano.common.model.Invoice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends CrudRepository<Invoice, Integer> {

    Optional<Invoice> findByNumber(String number);
    Optional<Invoice> findByUserIdAndNumber(String userId, String number);
}
