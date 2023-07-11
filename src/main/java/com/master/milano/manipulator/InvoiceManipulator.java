package com.master.milano.manipulator;

import com.master.milano.common.dto.InvoiceDTO;
import com.master.milano.common.model.Invoice;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@Component
public class InvoiceManipulator {

    public Invoice dtoToModel(InvoiceDTO invoice) {
        return Invoice.builder()
                .balance(invoice.getBalance())
                .createdAt(Timestamp.from(Instant.now()))
                .number(invoice.getNumber())
                .publicId(UUID.randomUUID())
                .userId(invoice.getUserId())
                .validUntil(invoice.getValidUntil())
                .transactions(new HashSet<>())
                .build();
    }

    public InvoiceDTO modelToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .balance(invoice.getBalance())
                .number(invoice.getNumber())
                .userId(invoice.getUserId())
                .validUntil(invoice.getValidUntil())
                .build();
    }

}
