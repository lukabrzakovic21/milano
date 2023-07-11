package com.master.milano.controller;

import com.master.milano.common.dto.ItemDTO;
import com.master.milano.common.dto.ItemInterestDTO;
import com.master.milano.common.dto.PurchaseDTO;
import com.master.milano.common.dto.UserWithInvoice;
import com.master.milano.common.model.Item;
import com.master.milano.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);


    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/create")
    public ResponseEntity<ItemDTO> createItem(@RequestBody ItemDTO itemDto) {
        logger.info("Create new item started with request: {}", itemDto);
        return ok(itemService.createItem(itemDto));
    }

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAll(
//            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
//            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
//            @RequestParam(name = "type", required = false, defaultValue = "NESTO") String type,
//            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,) {
    ){
        logger.info("Get all items started.");
        return ok(itemService.getAllItems());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ItemDTO> getByPublicId(@PathVariable String publicId) {
        logger.info("Get item with id: {}", publicId);
        return ok(itemService.getByPublicId(UUID.fromString(publicId)));
    }

    @DeleteMapping("temp/{publicId}")
    public ResponseEntity<ItemDTO> removeItemTemporarily(@PathVariable String publicId) {
        logger.info("Removing item temporarily with id: {}", publicId);

        return ok(itemService.removeItem(UUID.fromString(publicId), true));
    }

    @DeleteMapping("perm/{publicId}")
    public ResponseEntity<ItemDTO> removeItemPermanently(@PathVariable String publicId) {
        logger.info("Removing item temporarily with id: {}", publicId);

        return ok(itemService.removeItem(UUID.fromString(publicId), false));
    }

    @PostMapping("/buy/{publicId}")
    public ResponseEntity<PurchaseDTO> buyItem(@PathVariable String publicId, @RequestBody UserWithInvoice request) {
        logger.info("User {} wants to buy item with publicId: {}", request.getUserId(), publicId);
        return ok(itemService.buyItem(UUID.fromString(publicId), request));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PurchaseDTO>> getPurchaseHistoryForUser(@PathVariable String userId) {
        logger.info("Getting purchase history for user: {} ", userId);
        return ok(itemService.getPurchaseHistory(userId));
    }

    @PostMapping("/interest")
    public ResponseEntity<ItemInterestDTO> interestInItem(@RequestBody ItemInterestDTO request) {
        logger.info("User {} wants is interesting in buying item with publicId: {}", request.getUserId(), request.getItemId());
        return ok(itemService.interestInItem(request.getUserId(), request.getItemId()));
    }

    @PatchMapping("/{publicId}")
    public ResponseEntity<ItemDTO> increaseNumberOfItems(@PathVariable String publicId, Integer increment ) {
        logger.info("Increase number of items for item with id: {} by increment: {}", publicId, increment);
        return ok(itemService.increaseNumberOfItems(publicId, increment));
    }

}
