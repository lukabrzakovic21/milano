package com.master.milano.controller;

import com.master.milano.common.dto.InvoiceDTO;
import com.master.milano.common.util.InvoiceNumberWithPrice;
import com.master.milano.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(path = "/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final static Logger logger = LoggerFactory.getLogger(InvoiceController.class);
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/create")
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestBody InvoiceDTO invoiceDTO) {

        logger.info("Started creating invoice: {}", invoiceDTO);
        return  ok(invoiceService.createInvoice(invoiceDTO));

    }

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<InvoiceDTO> getByInvoiceNumber(@PathVariable String invoiceNumber) {

        return ok(invoiceService.getByInvoiceNumber(invoiceNumber));
    }

    @PatchMapping("increase/{invoiceNumber}")
    public ResponseEntity<InvoiceDTO> increaseInvoiceBalance(@PathVariable String invoiceNumber,
                                                             @RequestBody InvoiceNumberWithPrice amount) {

        return ok(invoiceService.increaseInvoiceBalance(invoiceNumber, amount.getAmount()));
    }

    @PatchMapping("withdraw/{invoiceNumber}")
    public ResponseEntity<InvoiceDTO> withdrawMoneyFromInvoice(@PathVariable String invoiceNumber,
                                                             @RequestBody InvoiceNumberWithPrice amount) {

        return ok(invoiceService.withdrawMoneyFromInvoice(invoiceNumber, amount.getAmount()));
    }
}
