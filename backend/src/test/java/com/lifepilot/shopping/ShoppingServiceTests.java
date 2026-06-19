package com.lifepilot.shopping;

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
import com.lifepilot.shopping.dto.CreateShoppingItemRequest;
import com.lifepilot.shopping.dto.CreateShoppingListRequest;
import com.lifepilot.shopping.dto.ShoppingItemResponse;
import com.lifepilot.shopping.dto.ShoppingListResponse;
import com.lifepilot.shopping.dto.UpdateShoppingItemRequest;
import com.lifepilot.shopping.dto.UpdateShoppingListRequest;
import com.lifepilot.space.HouseholdService;

@ExtendWith(MockitoExtension.class)
class ShoppingServiceTests {

    @Mock
    private ShoppingListMapper listMapper;
    @Mock
    private ShoppingItemMapper itemMapper;
    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private ShoppingService shoppingService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long LIST_ID = 200L;
    private static final Long ITEM_ID = 300L;

    // ---- Shopping List CRUD ----

    @Test
    void createList_success() {
        CreateShoppingListRequest request = new CreateShoppingListRequest("周末购物", new BigDecimal("500"));

        ShoppingListResponse response = shoppingService.createList(USER_ID, SPACE_ID, request);

        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(listMapper).insert((ShoppingList) any());
        assertNotNull(response);
    }

    @Test
    void listLists_returnsResults() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);
        list.setName("购物清单");
        list.setStatus("active");
        list.setCreatedAt(LocalDateTime.now());

        when(listMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(list));

        List<ShoppingListResponse> result = shoppingService.listLists(USER_ID, SPACE_ID);

        assertEquals(1, result.size());
    }

    @Test
    void getList_notFound_throwsException() {
        when(listMapper.selectById(LIST_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> shoppingService.getList(USER_ID, SPACE_ID, LIST_ID));
    }

    @Test
    void getList_wrongSpace_throwsException() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(999L);

        when(listMapper.selectById(LIST_ID)).thenReturn(list);

        assertThrows(BusinessException.class,
                () -> shoppingService.getList(USER_ID, SPACE_ID, LIST_ID));
    }

    @Test
    void getList_found_returnsWithItems() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);
        list.setName("购物清单");
        list.setStatus("active");
        list.setCreatedAt(LocalDateTime.now());

        ShoppingItem item = new ShoppingItem();
        item.setId(ITEM_ID);
        item.setShoppingListId(LIST_ID);
        item.setName("牛奶");
        item.setPurchased(false);

        when(listMapper.selectById(LIST_ID)).thenReturn(list);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(item));

        ShoppingListResponse result = shoppingService.getList(USER_ID, SPACE_ID, LIST_ID);

        assertNotNull(result);
    }

    @Test
    void updateList_success() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);
        list.setName("旧名称");
        list.setStatus("active");

        when(listMapper.selectById(LIST_ID)).thenReturn(list);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest("新名称", "completed", null);

        ShoppingListResponse result = shoppingService.updateList(USER_ID, SPACE_ID, LIST_ID, request);

        assertNotNull(result);
        verify(listMapper).updateById((ShoppingList) any());
    }

    @Test
    void deleteList_success_deletesItemsAndList() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);

        when(listMapper.selectById(LIST_ID)).thenReturn(list);

        shoppingService.deleteList(USER_ID, SPACE_ID, LIST_ID);

        verify(itemMapper).delete(any(LambdaQueryWrapper.class));
        verify(listMapper).deleteById((Long) eq(LIST_ID));
    }

    @Test
    void deleteList_notFound_throwsException() {
        when(listMapper.selectById(LIST_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> shoppingService.deleteList(USER_ID, SPACE_ID, LIST_ID));
    }

    // ---- Shopping Item CRUD ----

    @Test
    void addItem_success() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);

        when(listMapper.selectById(LIST_ID)).thenReturn(list);

        CreateShoppingItemRequest request = new CreateShoppingItemRequest(
                "苹果", new BigDecimal("5"), "kg", new BigDecimal("10")
        );

        ShoppingItemResponse result = shoppingService.addItem(USER_ID, SPACE_ID, LIST_ID, request);

        assertNotNull(result);
        verify(itemMapper).insert((ShoppingItem) any());
    }

    @Test
    void updateItem_itemNotFound_throwsException() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);

        when(listMapper.selectById(LIST_ID)).thenReturn(list);
        when(itemMapper.selectById(ITEM_ID)).thenReturn(null);

        UpdateShoppingItemRequest request = new UpdateShoppingItemRequest(
                null, null, null, null, true
        );

        assertThrows(BusinessException.class,
                () -> shoppingService.updateItem(USER_ID, SPACE_ID, LIST_ID, ITEM_ID, request));
    }

    @Test
    void updateItem_wrongList_throwsException() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);

        ShoppingItem item = new ShoppingItem();
        item.setId(ITEM_ID);
        item.setShoppingListId(999L); // different list

        when(listMapper.selectById(LIST_ID)).thenReturn(list);
        when(itemMapper.selectById(ITEM_ID)).thenReturn(item);

        UpdateShoppingItemRequest request = new UpdateShoppingItemRequest(
                null, null, null, null, true
        );

        assertThrows(BusinessException.class,
                () -> shoppingService.updateItem(USER_ID, SPACE_ID, LIST_ID, ITEM_ID, request));
    }

    @Test
    void deleteItem_success() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);

        ShoppingItem item = new ShoppingItem();
        item.setId(ITEM_ID);
        item.setShoppingListId(LIST_ID);

        when(listMapper.selectById(LIST_ID)).thenReturn(list);
        when(itemMapper.selectById(ITEM_ID)).thenReturn(item);

        shoppingService.deleteItem(USER_ID, SPACE_ID, LIST_ID, ITEM_ID);

        verify(itemMapper).deleteById((Long) eq(ITEM_ID));
    }

    @Test
    void deleteItem_notFound_throwsException() {
        ShoppingList list = new ShoppingList();
        list.setId(LIST_ID);
        list.setHouseholdId(SPACE_ID);

        when(listMapper.selectById(LIST_ID)).thenReturn(list);
        when(itemMapper.selectById(ITEM_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> shoppingService.deleteItem(USER_ID, SPACE_ID, LIST_ID, ITEM_ID));
    }
}