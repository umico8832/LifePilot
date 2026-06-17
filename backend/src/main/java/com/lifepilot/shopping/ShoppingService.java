package com.lifepilot.shopping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.shopping.dto.CreateShoppingItemRequest;
import com.lifepilot.shopping.dto.CreateShoppingListRequest;
import com.lifepilot.shopping.dto.ShoppingItemResponse;
import com.lifepilot.shopping.dto.ShoppingListResponse;
import com.lifepilot.shopping.dto.UpdateShoppingItemRequest;
import com.lifepilot.shopping.dto.UpdateShoppingListRequest;
import com.lifepilot.space.HouseholdService;

@Service
public class ShoppingService {

    private final ShoppingListMapper listMapper;
    private final ShoppingItemMapper itemMapper;
    private final HouseholdService householdService;

    public ShoppingService(ShoppingListMapper listMapper, ShoppingItemMapper itemMapper, HouseholdService householdService) {
        this.listMapper = listMapper;
        this.itemMapper = itemMapper;
        this.householdService = householdService;
    }

    // ---- Shopping List CRUD ----

    @Transactional
    public ShoppingListResponse createList(Long userId, Long spaceId, CreateShoppingListRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        ShoppingList list = new ShoppingList();
        list.setHouseholdId(spaceId);
        list.setName(request.name());
        list.setStatus("active");
        list.setEstimatedBudget(request.estimatedBudget());
        list.setCreatedBy(userId);
        LocalDateTime now = LocalDateTime.now();
        list.setCreatedAt(now);
        list.setUpdatedAt(now);

        listMapper.insert(list);
        return ShoppingListResponse.from(list);
    }

    public List<ShoppingListResponse> listLists(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<ShoppingList> lists = listMapper.selectList(
                new LambdaQueryWrapper<ShoppingList>()
                        .eq(ShoppingList::getHouseholdId, spaceId)
                        .orderByDesc(ShoppingList::getCreatedAt)
        );

        return lists.stream().map(ShoppingListResponse::from).collect(Collectors.toList());
    }

    public ShoppingListResponse getList(Long userId, Long spaceId, Long listId) {
        householdService.requireSpaceMembership(userId, spaceId);

        ShoppingList list = listMapper.selectById(listId);
        if (list == null || !list.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Shopping list not found");
        }

        List<ShoppingItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<ShoppingItem>()
                        .eq(ShoppingItem::getShoppingListId, listId)
                        .orderByAsc(ShoppingItem::getId)
        );

        List<ShoppingItemResponse> itemResponses = items.stream()
                .map(ShoppingItemResponse::from)
                .collect(Collectors.toList());

        return ShoppingListResponse.from(list, itemResponses);
    }

    @Transactional
    public ShoppingListResponse updateList(Long userId, Long spaceId, Long listId, UpdateShoppingListRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        ShoppingList list = listMapper.selectById(listId);
        if (list == null || !list.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Shopping list not found");
        }

        if (request.name() != null) list.setName(request.name());
        if (request.status() != null) list.setStatus(request.status());
        if (request.estimatedBudget() != null) list.setEstimatedBudget(request.estimatedBudget());
        list.setUpdatedAt(LocalDateTime.now());

        listMapper.updateById(list);
        return ShoppingListResponse.from(list);
    }

    @Transactional
    public void deleteList(Long userId, Long spaceId, Long listId) {
        householdService.requireSpaceMembership(userId, spaceId);

        ShoppingList list = listMapper.selectById(listId);
        if (list == null || !list.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Shopping list not found");
        }

        // Delete all items in the list first
        itemMapper.delete(
                new LambdaQueryWrapper<ShoppingItem>()
                        .eq(ShoppingItem::getShoppingListId, listId)
        );

        listMapper.deleteById(listId);
    }

    // ---- Shopping Item CRUD ----

    @Transactional
    public ShoppingItemResponse addItem(Long userId, Long spaceId, Long listId, CreateShoppingItemRequest request) {
        ShoppingList list = requireListMembership(userId, spaceId, listId);

        ShoppingItem item = new ShoppingItem();
        item.setShoppingListId(listId);
        item.setName(request.name());
        item.setQuantity(request.quantity() != null ? request.quantity() : java.math.BigDecimal.ONE);
        item.setUnit(request.unit());
        item.setEstimatedPrice(request.estimatedPrice());
        item.setPurchased(false);
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        itemMapper.insert(item);
        list.setUpdatedAt(now);
        listMapper.updateById(list);

        return ShoppingItemResponse.from(item);
    }

    @Transactional
    public ShoppingItemResponse updateItem(Long userId, Long spaceId, Long listId, Long itemId, UpdateShoppingItemRequest request) {
        ShoppingList list = requireListMembership(userId, spaceId, listId);

        ShoppingItem item = itemMapper.selectById(itemId);
        if (item == null || !item.getShoppingListId().equals(listId)) {
            throw new BusinessException("NOT_FOUND", "Shopping item not found");
        }

        if (request.name() != null) item.setName(request.name());
        if (request.quantity() != null) item.setQuantity(request.quantity());
        if (request.unit() != null) item.setUnit(request.unit());
        if (request.estimatedPrice() != null) item.setEstimatedPrice(request.estimatedPrice());
        if (request.purchased() != null) item.setPurchased(request.purchased());
        item.setUpdatedAt(LocalDateTime.now());

        itemMapper.updateById(item);
        list.setUpdatedAt(LocalDateTime.now());
        listMapper.updateById(list);

        return ShoppingItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(Long userId, Long spaceId, Long listId, Long itemId) {
        ShoppingList list = requireListMembership(userId, spaceId, listId);

        ShoppingItem item = itemMapper.selectById(itemId);
        if (item == null || !item.getShoppingListId().equals(listId)) {
            throw new BusinessException("NOT_FOUND", "Shopping item not found");
        }

        itemMapper.deleteById(itemId);
        list.setUpdatedAt(LocalDateTime.now());
        listMapper.updateById(list);
    }

    // ---- helpers ----

    private ShoppingList requireListMembership(Long userId, Long spaceId, Long listId) {
        householdService.requireSpaceMembership(userId, spaceId);

        ShoppingList list = listMapper.selectById(listId);
        if (list == null || !list.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Shopping list not found");
        }
        return list;
    }
}