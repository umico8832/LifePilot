package com.lifepilot.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.inventory.dto.CreateInventoryItemRequest;
import com.lifepilot.inventory.dto.InventoryItemResponse;
import com.lifepilot.inventory.dto.UpdateInventoryItemRequest;
import com.lifepilot.space.HouseholdService;

@Service
public class InventoryService {

    private final InventoryItemMapper itemMapper;
    private final HouseholdService householdService;

    public InventoryService(InventoryItemMapper itemMapper, HouseholdService householdService) {
        this.itemMapper = itemMapper;
        this.householdService = householdService;
    }

    @Transactional
    public InventoryItemResponse createItem(Long userId, Long spaceId, CreateInventoryItemRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        InventoryItem item = new InventoryItem();
        item.setHouseholdId(spaceId);
        item.setName(request.name());
        item.setCategory(request.category());
        item.setQuantity(request.quantity() != null ? request.quantity() : BigDecimal.ZERO);
        item.setUnit(request.unit());
        item.setLocation(request.location());
        item.setExpireAt(request.expireAt());
        item.setLowStockThreshold(request.lowStockThreshold());
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        itemMapper.insert(item);
        return InventoryItemResponse.from(item);
    }

    public List<InventoryItemResponse> listItems(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<InventoryItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId)
                        .orderByDesc(InventoryItem::getCreatedAt)
        );

        return items.stream().map(InventoryItemResponse::from).collect(Collectors.toList());
    }

    public List<InventoryItemResponse> listLowStockAlerts(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<InventoryItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InventoryItem>()
                        .eq(InventoryItem::getHouseholdId, spaceId)
                        .isNotNull(InventoryItem::getLowStockThreshold)
                        .apply("quantity <= low_stock_threshold")
                        .orderByAsc(InventoryItem::getExpireAt)
        );

        return items.stream().map(InventoryItemResponse::from).collect(Collectors.toList());
    }

    public InventoryItemResponse getItem(Long userId, Long spaceId, Long itemId) {
        householdService.requireSpaceMembership(userId, spaceId);

        InventoryItem item = itemMapper.selectById(itemId);
        if (item == null || !item.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Inventory item not found");
        }

        return InventoryItemResponse.from(item);
    }

    @Transactional
    public InventoryItemResponse updateItem(Long userId, Long spaceId, Long itemId, UpdateInventoryItemRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        InventoryItem item = itemMapper.selectById(itemId);
        if (item == null || !item.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Inventory item not found");
        }

        if (request.name() != null) item.setName(request.name());
        if (request.category() != null) item.setCategory(request.category());
        if (request.quantity() != null) item.setQuantity(request.quantity());
        if (request.unit() != null) item.setUnit(request.unit());
        if (request.location() != null) item.setLocation(request.location());
        if (request.expireAt() != null) item.setExpireAt(request.expireAt());
        if (request.lowStockThreshold() != null) item.setLowStockThreshold(request.lowStockThreshold());
        item.setUpdatedAt(LocalDateTime.now());

        itemMapper.updateById(item);
        return InventoryItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(Long userId, Long spaceId, Long itemId) {
        householdService.requireSpaceMembership(userId, spaceId);

        InventoryItem item = itemMapper.selectById(itemId);
        if (item == null || !item.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Inventory item not found");
        }

        itemMapper.deleteById(itemId);
    }
}