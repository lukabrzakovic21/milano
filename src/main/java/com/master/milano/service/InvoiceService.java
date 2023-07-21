package com.master.milano.service;

import com.master.milano.common.dto.InvoiceDTO;
import com.master.milano.common.model.Invoice;
import com.master.milano.common.model.Transaction;
import com.master.milano.common.util.TransactionReason;
import com.master.milano.exception.invoice.InvoiceInsufficientFundsException;
import com.master.milano.exception.invoice.InvoiceNotFoundException;
import com.master.milano.exception.util.UnauthorizedException;
import com.master.milano.manipulator.InvoiceManipulator;
import com.master.milano.repository.InvoiceRepository;
import com.master.milano.validator.InvoiceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceManipulator invoiceManipulator, InvoiceValidator invoiceValidator) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceManipulator = invoiceManipulator;
        this.invoiceValidator = invoiceValidator;
    }

    public InvoiceDTO createInvoice(InvoiceDTO invoiceDTO, String authorizationHeader) {

        logger.info("Create invoice with request: {}", invoiceDTO);
        invoiceValidator.validateInvoice(invoiceDTO, authorizationHeader);
        logger.info("Validation of previous request passed successfully.");
        var invoice = invoiceManipulator.dtoToModel(invoiceDTO);

        var savedInvoice = invoiceRepository.save(invoice);
        logger.info("Invoice with number {} successfully saved into db.", savedInvoice.getNumber());
        return invoiceManipulator.modelToDTO(savedInvoice);

    }

    public InvoiceDTO getByInvoiceNumber(String invoiceNumber, String sessionUserId) {

        var invoice  = invoiceRepository.findByNumber(invoiceNumber);

        if(invoice.isEmpty()) {
            logger.warn("Invoice with number {} doesn't exist in the db.", invoiceNumber);
            throw new InvoiceNotFoundException("Invoice with this number does not exist.");
        }
        if(!invoice.get().getUserId().equalsIgnoreCase(sessionUserId)) {
            logger.warn("User cannot access invoice that is assigned to other users");
            throw new UnauthorizedException("User cannot access invoice that is assigned to other users");
        }

        return invoiceManipulator.modelToDTO(invoice.get());
    }

    public InvoiceDTO withdrawMoneyFromInvoice(String invoiceNumber, BigDecimal amount, String sessionUserId) {

        var invoice  = invoiceRepository.findByNumber(invoiceNumber);

        if(invoice.isEmpty()) {
            logger.warn("Invoice with number {} doesn't exist in the db.", invoiceNumber);
            throw new InvoiceNotFoundException("Invoice with this number does not exist.");
        }

        var invoiceWithInfo = invoice.get();

        if(!invoice.get().getUserId().equalsIgnoreCase(sessionUserId)) {
            logger.warn("User cannot access invoice that is assigned to other users");
            throw new UnauthorizedException("User cannot access invoice that is assigned to other users");
        }
        if(invoiceWithInfo.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) == -1) {
            logger.warn("Invoice with number {} doesn't have enough funds.", invoiceNumber);
            throw new InvoiceInsufficientFundsException(String.format("Invoice with number %s doesn't have enough funds.", invoiceNumber));
        }
        invoiceWithInfo.setBalance(invoiceWithInfo.getBalance().subtract(amount));

        var transaction  =  addTransaction(invoiceWithInfo, amount, TransactionReason.INVOICE_WITHDRAW);
        invoiceWithInfo.getTransactions().add(transaction);

        var savedInvoice = invoiceRepository.save(invoiceWithInfo);
        logger.info("Invoice successfully saved into db.");
        //add transaction after adding transaction types
        return invoiceManipulator.modelToDTO(savedInvoice);

    }

    public InvoiceDTO increaseInvoiceBalance(String invoiceNumber, BigDecimal amount, String sessionUserId) {

        var invoice  = invoiceRepository.findByNumber(invoiceNumber);

        if(invoice.isEmpty()) {
            logger.warn("Invoice with number {} doesn't exist in the db.", invoiceNumber);
            throw new InvoiceNotFoundException("Invoice with this number does not exist.");
        }

        var invoiceWithInfo = invoice.get();
        if(!invoiceWithInfo.getUserId().equalsIgnoreCase(sessionUserId)) {
            logger.warn("User cannot access invoice that is assigned to other users");
            throw new UnauthorizedException("User cannot access invoice that is assigned to other users");
        }
        invoiceWithInfo.setBalance(invoiceWithInfo.getBalance().add(amount));

        var transaction  =  addTransaction(invoiceWithInfo, amount, TransactionReason.INVOICE_PAYMENT);
        invoiceWithInfo.getTransactions().add(transaction);

        var savedInvoice = invoiceRepository.save(invoiceWithInfo);
        logger.info("Invoice successfully saved into db.");
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
