package com.master.milano.validator;

import com.master.milano.client.IstanbulClient;
import com.master.milano.common.dto.InvoiceDTO;
import com.master.milano.common.dto.UserDTO;
import com.master.milano.exception.invoice.InvoiceBadRequest;
import com.master.milano.exception.item.ItemBadRequest;
import com.master.milano.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InvoiceValidator {

    private final InvoiceRepository invoiceRepository;
    private final IstanbulClient istanbul;

    public InvoiceValidator(InvoiceRepository invoiceRepository, IstanbulClient istanbul) {
        this.invoiceRepository = invoiceRepository;
        this.istanbul = istanbul;
    }

    public void validateInvoice(InvoiceDTO invoiceDTO) {

            if(Objects.isNull(invoiceDTO) || Objects.isNull(invoiceDTO.getNumber()) || Objects.isNull(invoiceDTO.getBalance())
                    || Objects.isNull(invoiceDTO.getValidUntil()) || Objects.isNull(invoiceDTO.getUserId())) {
                throw new InvoiceBadRequest("All fields are mandatory");
            }
            if(invoiceDTO.getNumber().isBlank() || invoiceDTO.getUserId().isBlank()) {
                throw new ItemBadRequest("All fields are mandatory");
            }

            if(checkIfInvoiceAlreadyExist(invoiceDTO.getNumber())) {
                throw new ItemBadRequest("Invoice with that number already exists.");
            }

        //check userExistence, call Istanbul service

        try {
            istanbul.getUserByPublicId(invoiceDTO.getUserId(), false);
        } catch (Exception exception) {
            throw new ItemBadRequest("User with this id doesn't exist");
        }
    }

    private boolean checkIfInvoiceAlreadyExist(String invoiceNumber) {
        var invoice = invoiceRepository.findByNumber(invoiceNumber);
        return invoice.isPresent();
    }
}
