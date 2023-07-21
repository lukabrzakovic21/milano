package com.master.milano.controller;

import com.master.milano.common.dto.ItemDTO;
import com.master.milano.common.dto.ItemInterestDTO;
import com.master.milano.common.dto.PurchaseDTO;
import com.master.milano.common.dto.UserWithInvoice;
import com.master.milano.common.util.JwtUtil;
import com.master.milano.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(path = "/item")
public class ItemController {

    private final ItemService itemService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);


    public ItemController(ItemService itemService, JwtUtil jwtUtil) {
        this.itemService = itemService;
        this.jwtUtil = jwtUtil;
    }

    //admin, prodavac
    @PostMapping("/create")
    public ResponseEntity<ItemDTO> createItem(@RequestBody ItemDTO itemDto) {
        logger.info("Create new item started with request: {}", itemDto);
        return ok(itemService.createItem(itemDto));
    }

    //dovrsi ovo
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAll(
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
            @RequestParam(name = "page_size", required = false, defaultValue = "0") Integer pageSize,
            @RequestParam(name = "sort_by", required = false, defaultValue = "id") String sortBy,
            @RequestParam(name = "sort_direction", required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(name = "filter_type", required = false, defaultValue = "") String type) {

        logger.info("Get all items started.");
        return ok(itemService.getAllItems(limit, pageSize, sortBy, sortDirection, type));
    }

    //svi
    @GetMapping("/{publicId}")
    public ResponseEntity<ItemDTO> getByPublicId(@PathVariable String publicId) {
        logger.info("Get item with id: {}", publicId);
        return ok(itemService.getByPublicId(UUID.fromString(publicId)));
    }

    //prodavac
    @DeleteMapping("temp/{publicId}")
    public ResponseEntity<ItemDTO> removeItemTemporarily(@PathVariable String publicId) {
        logger.info("Removing item temporarily with id: {}", publicId);

        return ok(itemService.removeItem(UUID.fromString(publicId), true));
    }

    //prodavac
    @DeleteMapping("perm/{publicId}")
    public ResponseEntity<ItemDTO> removeItemPermanently(@PathVariable String publicId) {
        logger.info("Removing item permanently with id: {}", publicId);

        return ok(itemService.removeItem(UUID.fromString(publicId), false));
    }

    //kupac
    @PostMapping("/buy/{publicId}")
    public ResponseEntity<PurchaseDTO> buyItem(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable String publicId, @RequestBody String invoiceNumber) {
        var authorizationHeader = authorization.replace("Bearer ", "");
        var jwtBody = jwtUtil.retrieveAllClaims(authorizationHeader);
        var sessionUserId = (String)jwtBody.get("public_id");
        logger.info("User {} wants to buy item with publicId: {}", sessionUserId, publicId);
        var userWithInvoice = UserWithInvoice.builder().userId(sessionUserId).invoiceNumber(invoiceNumber).build();
        return ok(itemService.buyItem(UUID.fromString(publicId), userWithInvoice, authorizationHeader ));
    }

    //kupac
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PurchaseDTO>> getPurchaseHistoryForUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable String userId) {
        logger.info("Getting purchase history for user: {} ", userId);
        var authorizationHeader = authorization.replace("Bearer ", "");
        var jwtBody = jwtUtil.retrieveAllClaims(authorizationHeader);
        var sessionUserId = (String)jwtBody.get("public_id");
        var role = (String)jwtBody.get("role");
        return ok(itemService.getPurchaseHistory(userId, sessionUserId, role));
    }

    //kupac
    @PostMapping("/interest/{itemId}")
    public ResponseEntity<ItemInterestDTO> interestInItem(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable String itemId) {
        var authorizationHeader = authorization.replace("Bearer ", "");
        var jwtBody = jwtUtil.retrieveAllClaims(authorizationHeader);
        var sessionUserId = (String)jwtBody.get("public_id");
        logger.info("User {} wants is interesting in buying item with publicId: {}", sessionUserId, itemId);
        return ok(itemService.interestInItem(sessionUserId, itemId, authorization));
    }

    //prodavac
    @PatchMapping("/{publicId}")
    public ResponseEntity<ItemDTO> increaseNumberOfItems(@PathVariable String publicId, Integer increment ) {
        logger.info("Increase number of items for item with id: {} by increment: {}", publicId, increment);
        return ok(itemService.increaseNumberOfItems(publicId, increment));
    }

}
