package com.master.milano.controller;

import com.master.milano.common.dto.InvoiceDTO;
import com.master.milano.common.util.InvoiceNumberWithPrice;
import com.master.milano.common.util.JwtUtil;
import com.master.milano.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(path = "/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final JwtUtil jwtUtil;
    private final static Logger logger = LoggerFactory.getLogger(InvoiceController.class);
    public InvoiceController(InvoiceService invoiceService, JwtUtil jwtUtil) {
        this.invoiceService = invoiceService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody InvoiceDTO invoiceDTO) {

        logger.info("Started creating invoice: {}", invoiceDTO);
        return  ok(invoiceService.createInvoice(invoiceDTO, authorization));

    }

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<InvoiceDTO> getByInvoiceNumber(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable String invoiceNumber) {
        logger.info("Get invoice by invoice number: {}", invoiceNumber);
        var authorizationHeader = authorization.replace("Bearer ", "");
        var jwtBody = jwtUtil.retrieveAllClaims(authorizationHeader);
        var sessionUserId = (String)jwtBody.get("public_id");
        return ok(invoiceService.getByInvoiceNumber(invoiceNumber, sessionUserId));
    }

    @PatchMapping("increase/{invoiceNumber}")
    public ResponseEntity<InvoiceDTO> increaseInvoiceBalance(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                             @PathVariable String invoiceNumber,
                                                             @RequestBody InvoiceNumberWithPrice amount) {

        logger.info("Increase invoice balance for {} dollars with invoice number {}", amount.getAmount(), invoiceNumber);
        var authorizationHeader = authorization.replace("Bearer ", "");
        var jwtBody = jwtUtil.retrieveAllClaims(authorizationHeader);
        var sessionUserId = (String)jwtBody.get("public_id");
        return ok(invoiceService.increaseInvoiceBalance(invoiceNumber, amount.getAmount(), sessionUserId));
    }

    @PatchMapping("withdraw/{invoiceNumber}")
    public ResponseEntity<InvoiceDTO> withdrawMoneyFromInvoice(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                              @PathVariable String invoiceNumber,
                                                             @RequestBody InvoiceNumberWithPrice amount) {
        logger.info("Withdraw money from invoice with invoice number {}. Amount: {}", invoiceNumber, amount.getAmount());
        var authorizationHeader = authorization.replace("Bearer ", "");
        var jwtBody = jwtUtil.retrieveAllClaims(authorizationHeader);
        var sessionUserId = (String)jwtBody.get("public_id");
        return ok(invoiceService.withdrawMoneyFromInvoice(invoiceNumber, amount.getAmount(), sessionUserId));
    }
}
