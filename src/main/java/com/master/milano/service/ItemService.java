package com.master.milano.service;

import com.master.milano.client.IstanbulClient;
import com.master.milano.common.dto.ItemDTO;
import com.master.milano.common.dto.ItemInterestDTO;
import com.master.milano.common.dto.PurchaseDTO;
import com.master.milano.common.dto.UserWithInvoice;
import com.master.milano.common.event.ItemAvailableAgain;
import com.master.milano.common.event.ItemNoLongerAvailable;
import com.master.milano.common.model.Invoice;
import com.master.milano.common.model.Item;
import com.master.milano.common.model.ItemInterest;
import com.master.milano.common.model.PurchaseHistory;
import com.master.milano.common.model.Transaction;
import com.master.milano.common.util.ItemType;
import com.master.milano.common.util.TransactionReason;
import com.master.milano.exception.invoice.InvoiceInsufficientFundsException;
import com.master.milano.exception.invoice.InvoiceNotFoundException;
import com.master.milano.exception.item.ItemBadRequest;
import com.master.milano.exception.item.ItemCanBeBoughtException;
import com.master.milano.exception.item.ItemNotFoundException;
import com.master.milano.exception.item.NoMoreItemsException;
import com.master.milano.exception.item.UserAlreadyInterestInItem;
import com.master.milano.exception.util.UnauthorizedException;
import com.master.milano.manipulator.ItemManipulator;
import com.master.milano.repository.InvoiceRepository;
import com.master.milano.repository.ItemInterestRepository;
import com.master.milano.repository.ItemRepository;
import com.master.milano.repository.PurchaseHistoryRepository;
import com.master.milano.validator.ItemValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ItemService {

    private final ItemValidator itemValidator;
    private final ItemRepository itemRepository;
    private final ItemManipulator itemManipulator;
    private final InvoiceRepository invoiceRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final ItemInterestRepository itemInterestRepository;
    private final IstanbulClient istanbul;
    private final RabbitMqService rabbitMqService;
    private final Logger logger = LoggerFactory.getLogger(ItemService.class);

    public ItemService(ItemValidator itemValidator, ItemRepository itemRepository,
                       ItemManipulator itemManipulator, InvoiceRepository invoiceRepository,
                       PurchaseHistoryRepository purchaseHistoryRepository, ItemInterestRepository itemInterestRepository,
                       IstanbulClient istanbul, RabbitMqService rabbitMqService) {
        this.itemValidator = itemValidator;
        this.itemRepository = itemRepository;
        this.itemManipulator = itemManipulator;
        this.invoiceRepository = invoiceRepository;
        this.purchaseHistoryRepository = purchaseHistoryRepository;
        this.itemInterestRepository = itemInterestRepository;
        this.istanbul = istanbul;
        this.rabbitMqService = rabbitMqService;}

    public ItemDTO createItem(ItemDTO itemDto) {


        itemValidator.validateNewItem(itemDto);
        logger.info("Item from request {} successfully validated.", itemDto);
        var item = itemManipulator.dtoToModel(itemDto);

        var savedItem = itemRepository.save(item);
        logger.info("Item from request {} saved into db.", itemDto);
        return itemManipulator.modelToDTO(savedItem);
    }


    public List<ItemDTO> getAllItems(Integer limit, Integer pageSize, String  sortBy, String sortDirection, String type) {
        itemValidator.validateParams(limit, pageSize, sortBy, sortDirection, type);
        logger.info("Get all items with params: limit = {}, pageSize = {}, sortBy = {}, sortDirection = {}, type = {}", limit, pageSize, sortBy, sortDirection, type);
        Page<Item> allItemsPageable = null;
        if(type.isEmpty()) {

            allItemsPageable = itemRepository.findAll(PageRequest.of(pageSize, limit, Sort.by(Sort.Direction.fromString(sortDirection), sortBy)));
        }
        else {
            allItemsPageable = itemRepository.findAllByType(PageRequest.of(pageSize, limit, Sort.by(Sort.Direction.fromString(sortDirection), sortBy)),
                    ItemType.fromString(type));
        }
        var itemsAsList = StreamSupport.stream(allItemsPageable.get().spliterator(), false)
                .map(itemManipulator::modelToDTO)
                .collect(Collectors.toList());
        return itemsAsList;
    }

    public ItemDTO getByPublicId(UUID publicId) {

        logger.info("Get item by id: {}", publicId);
        var item = itemRepository.findByPublicId(publicId);
        if(item.isEmpty()) {
            logger.warn("Item with provided id: {} does not exist.", publicId);
            throw new ItemNotFoundException(String.format("Item with provided id:%s does not exist.", publicId));
        }
        return itemManipulator.modelToDTO(item.get());
    }

    public ItemDTO removeItem(UUID publicId, boolean temporarily) {

        var item = itemRepository.findByPublicId(publicId);
        if(item.isEmpty()) {
            logger.warn("Item with provided id: {} does not exist.", publicId);
            throw new ItemNotFoundException(String.format("Item with provided id:%s does not exist.", publicId));
        }
        var foundedItem = item.get();

        foundedItem.setNumberLeft(temporarily ? 0 : -1);
        itemRepository.save(foundedItem);
        logger.info("Item with id: {} removed from the system", publicId);
        return itemManipulator.modelToDTO(foundedItem);
    }

    public PurchaseDTO buyItem(UUID publicId, UserWithInvoice request, String authorizationHeader) {

        var item = itemRepository.findByPublicId(publicId);
        if(item.isEmpty()) {
            logger.warn("Item with provided id: {} does not exist.", publicId);
            throw new ItemNotFoundException(String.format("Item with provided id:%s does not exist.", publicId));
        }

        var foundedItem = item.get();

        if(foundedItem.getNumberLeft()<=0) {
            logger.warn("There is no more items in stock with provided id: {}", publicId);
            throw new NoMoreItemsException("There is no items in stock");
        }
        var invoice  = invoiceRepository
                .findByUserIdAndNumber(request.getUserId(), request.getInvoiceNumber());

        if(invoice.isEmpty()) {
            logger.warn("Invoice with this number or userId does not exist.");
            throw new InvoiceNotFoundException("Invoice with this number or userId does not exist.");
        }

        var invoiceWithInfo = invoice.get();

        if(!invoiceWithInfo.getUserId().equalsIgnoreCase(request.getUserId())) {
            logger.warn("User cannot access invoice that is assigned to other users");
            throw new UnauthorizedException("User cannot access invoice that is assigned to other users");
        }

        if(invoiceWithInfo.getBalance().compareTo(foundedItem.getPrice())==-1) {
            logger.warn("User with id {} doesn't have enough money to buy item with id: {}", request.getUserId(), publicId);
            throw new InvoiceInsufficientFundsException(String.format("User with id %s doesn't have enough money to buy item with id: %s", request.getUserId(), publicId.toString()));
        }

        try {
            istanbul.getUserByPublicId(request.getUserId(), false, authorizationHeader);
        } catch (Exception exception) {
            logger.warn("User with this id: {} doesn't exist", request.getUserId());
            throw new ItemBadRequest("User with this id doesn't exist");
        }
        //everything is ok, buy new item
        var transaction = addTransaction(invoiceWithInfo, foundedItem.getPrice(), TransactionReason.ITEM_BUYOUT);
        var purchaseHistory = addPurchaseHistory(foundedItem, request.getUserId());

        invoiceWithInfo.setBalance(invoiceWithInfo.getBalance().subtract(foundedItem.getPrice()));
        foundedItem.setNumberLeft(foundedItem.getNumberLeft() - 1);


        invoiceWithInfo.getTransactions().add(transaction);
        foundedItem.getPurchaseHistories().add(purchaseHistory);

        invoiceRepository.save(invoiceWithInfo);
        itemRepository.save(foundedItem);

        logger.info("User with id {} successfully bought item with id: {}", request.getUserId(), publicId);
        if(foundedItem.getNumberLeft()==0) {
            logger.info("Sending messages to rabbitMq");
            rabbitMqService.itemNoMoreAvailable(ItemNoLongerAvailable.builder().item(foundedItem.getName()).build());
        }
        return PurchaseDTO.builder()
                .itemName(foundedItem.getName())
                .userId(request.getUserId())
                .date(transaction.getDateExecuted())
                .build();
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

    private PurchaseHistory addPurchaseHistory(Item item, String userId) {

        return PurchaseHistory.builder()
                .item(item)
                .date(Timestamp.from(Instant.now()))
                .userId(userId)
                .build();
    }

    public List<PurchaseDTO> getPurchaseHistory(String userId, String sessionUserId, String role) {
        if(!"ADMIN".equalsIgnoreCase(role) && !sessionUserId.equalsIgnoreCase(userId)) {
            logger.warn("User cannot access invoice that is assigned to other users");
            throw new UnauthorizedException("User cannot access information about other users.");
        }
        var history = purchaseHistoryRepository.getAllByUserId(userId);
        var historyAsList = StreamSupport.stream(history.spliterator(), false)
                .map(this::fromModel)
                .collect(Collectors.toList());
        logger.info("Returning history for user with id: {}", userId);
        return historyAsList;
    }

    private PurchaseDTO fromModel(PurchaseHistory model) {
        return PurchaseDTO.builder()
                .itemName(model.getItem().getName())
                .userId(model.getUserId())
                .date(model.getDate())
                .build();
    }

    public ItemInterestDTO interestInItem(String userId, String itemId, String authorizationHeader) {

        var item = itemRepository.findByPublicId(UUID.fromString(itemId));
        if(item.isEmpty()) {
            logger.warn("Item with provided id: {} does not exist.", itemId);
            throw new ItemNotFoundException(String.format("Item with provided id:%s does not exist.", itemId));
        }
        var foundedItem = item.get();

        if(foundedItem.getNumberLeft()>0) {
            logger.warn("Item with provided id: {} is in stock. You can try to buy it.", itemId);

            throw new ItemCanBeBoughtException("This item is in stock. You can try to buy it.");
        }

        if(foundedItem.getNumberLeft()==-1) {
            logger.warn("Item with provided id: {} is removed permanently", itemId);

            throw new ItemNotFoundException("This item is removed permanently. It cannot be bought again.");
        }

        var interest = itemInterestRepository.findByUserIdAndItem(userId, foundedItem);
        if(interest.isPresent()) {
            logger.warn("User with id {} has already been interested in the item with id: {}", userId, itemId);

            throw new UserAlreadyInterestInItem("This user has already been interested in this item");
        }

        //check user existence in istanbul service
        try {
            istanbul.getUserByPublicId(userId, false, authorizationHeader);
        } catch (Exception exception) {
            logger.warn("User with id: {} doesn't exist in the system.", userId);
            throw new ItemBadRequest("User with this id doesn't exist");
        }

        var itemInterest = addItemInterest(foundedItem, userId);

        foundedItem.getItemInterest().add(itemInterest);

        itemRepository.save(foundedItem);

        logger.info("Successfully saved combination user: {} - item: {} ", userId, itemId);
        return ItemInterestDTO.builder()
                .itemId(itemId)
                .userId(userId)
                .build();
    }

    private ItemInterest addItemInterest(Item item, String userId) {
        return ItemInterest.builder()
                .item(item)
                .userId(userId)
                .build();
    }

    public ItemDTO increaseNumberOfItems(String publicId, Integer increment) {

        var item = itemRepository.findByPublicId(UUID.fromString(publicId));
        if(item.isEmpty()) {
            logger.warn("Item with provided id: {} does not exist.", publicId);
            throw new ItemNotFoundException(String.format("Item with provided id:%s does not exist.", publicId));
        }
        var foundedItem = item.get();

        if(foundedItem.getNumberLeft()==-1) {
            logger.warn("Item with provided id: {} is removed permanently.", publicId);
            throw new ItemNotFoundException("This item is removed permanently. It is impossible to increase number of this item");
        }

        var previousNumberLeft  = foundedItem.getNumberLeft();
        foundedItem.setNumberLeft(foundedItem.getNumberLeft() + increment);
        itemRepository.save(foundedItem);

        if(previousNumberLeft==0) {
            var allUsersForItem = itemInterestRepository.findAllByPublicId(UUID.fromString(publicId));
            var listOfIds = allUsersForItem.stream().map(itemInterest -> UUID.fromString(itemInterest.getUserId())).collect(Collectors.toList());
            Iterable<Long> idsForDelete = (Iterable<Long>) allUsersForItem.stream().map(itemInterest -> itemInterest.getId()).iterator();
            var usersWithEmails = istanbul.getAllUsersForPublicIds(listOfIds);
            var userEmails = usersWithEmails.stream().map(info -> info.getEmail()).collect(Collectors.toList());
            var itemAvailable = ItemAvailableAgain.builder()
                    .item(foundedItem.getName())
                    .emails(userEmails)
                    .build();
            logger.info("Send message via rabbitMq");
            rabbitMqService.itemAvailableAgain(itemAvailable);
            //delete all users that were interested into this itme
            //verify if this actually work
            itemInterestRepository.deleteAllById(idsForDelete);
        }
        logger.info("Successfully increased number for item with id: {}", publicId);
        return itemManipulator.modelToDTO(foundedItem);
    }
}
