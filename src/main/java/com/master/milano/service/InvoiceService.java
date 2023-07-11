package com.master.milano.service;

import com.master.milano.common.dto.InvoiceDTO;
import com.master.milano.common.model.Invoice;
import com.master.milano.common.model.Transaction;
import com.master.milano.common.util.TransactionReason;
import com.master.milano.exception.invoice.InvoiceInsufficientFundsException;
import com.master.milano.exception.invoice.InvoiceNotFoundException;
import com.master.milano.manipulator.InvoiceManipulator;
import com.master.milano.repository.InvoiceRepository;
import com.master.milano.validator.InvoiceValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceManipulator invoiceManipulator;
    private final InvoiceValidator invoiceValidator;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceManipulator invoiceManipulator, InvoiceValidator invoiceValidator) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceManipulator = invoiceManipulator;
        this.invoiceValidator = invoiceValidator;
    }

    public InvoiceDTO createInvoice(InvoiceDTO invoiceDTO) {

        invoiceValidator.validateInvoice(invoiceDTO);

        var invoice = invoiceManipulator.dtoToModel(invoiceDTO);

        var savedInvoice = invoiceRepository.save(invoice);

        return invoiceManipulator.modelToDTO(savedInvoice);

    }

    public InvoiceDTO getByInvoiceNumber(String invoiceNumber) {

        var invoice  = invoiceRepository.findByNumber(invoiceNumber);

        if(invoice.isEmpty()) {
            throw new InvoiceNotFoundException("Invoice with this number does not exist.");
        }

        return invoiceManipulator.modelToDTO(invoice.get());
    }

    public InvoiceDTO withdrawMoneyFromInvoice(String invoiceNumber, BigDecimal amount) {

        var invoice  = invoiceRepository.findByNumber(invoiceNumber);

        if(invoice.isEmpty()) {
            throw new InvoiceNotFoundException("Invoice with this number does not exist.");
        }

        var invoiceWithInfo = invoice.get();
        if(invoiceWithInfo.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) == -1) {

            throw new InvoiceInsufficientFundsException(String.format("Invoice with number %s doesn't have enough funds.", invoiceNumber));

        }
        invoiceWithInfo.setBalance(invoiceWithInfo.getBalance().subtract(amount));

        var transaction  =  addTransaction(invoiceWithInfo, amount, TransactionReason.INVOICE_WITHDRAW);
        invoiceWithInfo.getTransactions().add(transaction);

        var savedInvoice = invoiceRepository.save(invoiceWithInfo);

        //add transaction after adding transaction types
        return invoiceManipulator.modelToDTO(savedInvoice);

    }

    public InvoiceDTO increaseInvoiceBalance(String invoiceNumber, BigDecimal amount) {

        var invoice  = invoiceRepository.findByNumber(invoiceNumber);

        if(invoice.isEmpty()) {
            throw new InvoiceNotFoundException("Invoice with this number does not exist.");
        }

        var invoiceWithInfo = invoice.get();
        invoiceWithInfo.setBalance(invoiceWithInfo.getBalance().add(amount));

        var transaction  =  addTransaction(invoiceWithInfo, amount, TransactionReason.INVOICE_PAYMENT);
        invoiceWithInfo.getTransactions().add(transaction);

        var savedInvoice = invoiceRepository.save(invoiceWithInfo);

        return invoiceManipulator.modelToDTO(savedInvoice);
    }

    private Transaction addTransaction(Invoice invoice, BigDecimal amount, TransactionReason reason) {

           return Transaction.builder()
                .publicId(UUID.randomUUID())
                .amount(amount)
                .dateExecuted(Timestamp.from(Instant.now()))
                .reason(reason)
                .invoice(invoice)
                .build();
    }
}
