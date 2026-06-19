package com.lifepilot.inventory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.inventory.dto.CreateInventoryItemRequest;
import com.lifepilot.inventory.dto.InventoryItemResponse;
import com.lifepilot.inventory.dto.UpdateInventoryItemRequest;
import com.lifepilot.space.HouseholdService;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTests {

    @Mock
    private InventoryItemMapper itemMapper;
    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private InventoryService inventoryService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long ITEM_ID = 50L;

    // --- createItem ---

    @Test
    void createItem_success() {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                "牛奶", "食品", new BigDecimal("10"), "盒", "冰箱",
                LocalDateTime.now().plusDays(7), new BigDecimal("3")
        );

        InventoryItemResponse response = inventoryService.createItem(USER_ID, SPACE_ID, request);

        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(itemMapper).insert((InventoryItem) any());
        assertNotNull(response);
    }

    @Test
    void createItem_defaultsQuantityToZero() {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                "面粉", "食品", null, "kg", "储物柜", null, null
        );

        InventoryItemResponse response = inventoryService.createItem(USER_ID, SPACE_ID, request);

        assertNotNull(response);
        verify(itemMapper).insert((InventoryItem) any());
    }

    @Test
    void createItem_nonMember_throwsException() {
        doThrow(new BusinessException("FORBIDDEN", "Not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                "物品", null, null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> inventoryService.createItem(USER_ID, SPACE_ID, request));
        verify(itemMapper, never()).insert((InventoryItem) any());
    }

    // --- listItems ---

    @Test
    void listItems_returnsResults() {
        InventoryItem item = new InventoryItem();
        item.setId(ITEM_ID);
        item.setHouseholdId(SPACE_ID);
        item.setName("牛奶");
        item.setCreatedAt(LocalDateTime.now());

        when(itemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(item));

        List<InventoryItemResponse> result = inventoryService.listItems(USER_ID, SPACE_ID);

        assertEquals(1, result.size());
    }

    @Test
    void listItems_empty_returnsEmptyList() {
        when(itemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<InventoryItemResponse> result = inventoryService.listItems(USER_ID, SPACE_ID);

        assertTrue(result.isEmpty());
    }

    // --- getItem ---

    @Test
    void getItem_found_returnsItem() {
        InventoryItem item = new InventoryItem();
        item.setId(ITEM_ID);
        item.setHouseholdId(SPACE_ID);
        item.setName("牛奶");

        when(itemMapper.selectById(ITEM_ID)).thenReturn(item);

        InventoryItemResponse result = inventoryService.getItem(USER_ID, SPACE_ID, ITEM_ID);

        assertNotNull(result);
    }

    @Test
    void getItem_notFound_throwsException() {
        when(itemMapper.selectById(ITEM_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> inventoryService.getItem(USER_ID, SPACE_ID, ITEM_ID));
    }

    @Test
    void getItem_wrongSpace_throwsException() {
        InventoryItem item = new InventoryItem();
        item.setId(ITEM_ID);
        item.setHouseholdId(999L);

        when(itemMapper.selectById(ITEM_ID)).thenReturn(item);

        assertThrows(BusinessException.class,
                () -> inventoryService.getItem(USER_ID, SPACE_ID, ITEM_ID));
    }

    // --- updateItem ---

    @Test
    void updateItem_success() {
        InventoryItem item = new InventoryItem();
        item.setId(ITEM_ID);
        item.setHouseholdId(SPACE_ID);
        item.setName("旧名称");
        item.setQuantity(new BigDecimal("5"));

        when(itemMapper.selectById(ITEM_ID)).thenReturn(item);

        UpdateInventoryItemRequest request = new UpdateInventoryItemRequest(
                "新名称", "日用品", new BigDecimal("20"), "个", "架子", null, new BigDecimal("5")
        );

        InventoryItemResponse result = inventoryService.updateItem(USER_ID, SPACE_ID, ITEM_ID, request);

        assertNotNull(result);
        verify(itemMapper).updateById((InventoryItem) any());
    }

    @Test
    void updateItem_notFound_throwsException() {
        when(itemMapper.selectById(ITEM_ID)).thenReturn(null);

        UpdateInventoryItemRequest request = new UpdateInventoryItemRequest(
                null, null, null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> inventoryService.updateItem(USER_ID, SPACE_ID, ITEM_ID, request));
        verify(itemMapper, never()).updateById((InventoryItem) any());
    }

    // --- deleteItem ---

    @Test
    void deleteItem_success() {
        InventoryItem item = new InventoryItem();
        item.setId(ITEM_ID);
        item.setHouseholdId(SPACE_ID);

        when(itemMapper.selectById(ITEM_ID)).thenReturn(item);

        inventoryService.deleteItem(USER_ID, SPACE_ID, ITEM_ID);

        verify(itemMapper).deleteById((Long) eq(ITEM_ID));
    }

    @Test
    void deleteItem_notFound_throwsException() {
        when(itemMapper.selectById(ITEM_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> inventoryService.deleteItem(USER_ID, SPACE_ID, ITEM_ID));
        verify(itemMapper, never()).deleteById((Long) any());
    }
}