package com.lifepilot.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifepilot.ai.dto.ParseShoppingRequest;
import com.lifepilot.ai.dto.ParseTodoRequest;
import com.lifepilot.ai.dto.ParseTransactionRequest;
import com.lifepilot.ai.dto.ShoppingDraftResponse;
import com.lifepilot.ai.dto.TodoDraftResponse;
import com.lifepilot.ai.dto.TransactionDraftResponse;
import com.lifepilot.common.BusinessException;
import com.lifepilot.finance.TransactionCategoryMapper;
import com.lifepilot.finance.TransactionRecordMapper;
import com.lifepilot.inventory.InventoryItemMapper;
import com.lifepilot.shopping.ShoppingListMapper;
import com.lifepilot.space.HouseholdService;
import com.lifepilot.todo.TodoTaskMapper;

@ExtendWith(MockitoExtension.class)
class AiServiceTests {

    @Mock
    private AiProvider aiProvider;
    @Mock
    private HouseholdService householdService;
    @Mock
    private TransactionRecordMapper transactionRecordMapper;
    @Mock
    private TransactionCategoryMapper transactionCategoryMapper;
    @Mock
    private InventoryItemMapper inventoryItemMapper;
    @Mock
    private ShoppingListMapper shoppingListMapper;
    @Mock
    private TodoTaskMapper todoTaskMapper;

    @InjectMocks
    private AiService aiService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;

    // --- parseTransaction ---

    @Test
    void parseTransaction_returnsProviderDraft() {
        TransactionDraftResponse draft = new TransactionDraftResponse(
                "expense", new BigDecimal("15.50"), "CNY", null, null, null,
                "早餐", false, "早餐15.5", null);
        when(aiProvider.parseTransaction("早餐15.5")).thenReturn(draft);

        TransactionDraftResponse result = aiService.parseTransaction(USER_ID, SPACE_ID,
                new ParseTransactionRequest("早餐15.5"));

        assertEquals("expense", result.type());
        assertEquals(new BigDecimal("15.50"), result.amount());
        assertFalse(result.needsReview());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
    }

    @Test
    void parseTransaction_returnsNeedsReviewOnNull() {
        when(aiProvider.parseTransaction("???")).thenReturn(null);

        TransactionDraftResponse result = aiService.parseTransaction(USER_ID, SPACE_ID,
                new ParseTransactionRequest("???"));

        assertTrue(result.needsReview());
        assertNotNull(result.validationMessage());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
    }

    @Test
    void parseTransaction_throwsWhenNotMember() {
        doThrow(new BusinessException("FORBIDDEN", "not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        assertThrows(BusinessException.class, () ->
                aiService.parseTransaction(USER_ID, SPACE_ID, new ParseTransactionRequest("test")));
    }

    // --- parseShoppingList ---

    @Test
    void parseShoppingList_returnsProviderDraft() {
        ShoppingDraftResponse draft = new ShoppingDraftResponse(
                "买菜", null, java.util.List.of(),
                false, "买菜", null);
        when(aiProvider.parseShoppingList("买菜")).thenReturn(draft);

        ShoppingDraftResponse result = aiService.parseShoppingList(USER_ID, SPACE_ID,
                new ParseShoppingRequest("买菜"));

        assertEquals("买菜", result.listName());
        assertFalse(result.needsReview());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
    }

    @Test
    void parseShoppingList_returnsNeedsReviewOnNull() {
        when(aiProvider.parseShoppingList("???")).thenReturn(null);

        ShoppingDraftResponse result = aiService.parseShoppingList(USER_ID, SPACE_ID,
                new ParseShoppingRequest("???"));

        assertTrue(result.needsReview());
        assertNotNull(result.validationMessage());
    }

    @Test
    void parseShoppingList_throwsWhenNotMember() {
        doThrow(new BusinessException("FORBIDDEN", "not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        assertThrows(BusinessException.class, () ->
                aiService.parseShoppingList(USER_ID, SPACE_ID, new ParseShoppingRequest("test")));
    }

    // --- parseTodo ---

    @Test
    void parseTodo_returnsProviderDraft() {
        TodoDraftResponse draft = new TodoDraftResponse(
                java.util.List.of(), false, "买牛奶", null);
        when(aiProvider.parseTodo("买牛奶")).thenReturn(draft);

        TodoDraftResponse result = aiService.parseTodo(USER_ID, SPACE_ID,
                new ParseTodoRequest("买牛奶"));

        assertFalse(result.needsReview());
        assertEquals("买牛奶", result.rawInput());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
    }

    @Test
    void parseTodo_returnsNeedsReviewOnNull() {
        when(aiProvider.parseTodo("???")).thenReturn(null);

        TodoDraftResponse result = aiService.parseTodo(USER_ID, SPACE_ID,
                new ParseTodoRequest("???"));

        assertTrue(result.needsReview());
        assertNotNull(result.validationMessage());
    }

    @Test
    void parseTodo_throwsWhenNotMember() {
        doThrow(new BusinessException("FORBIDDEN", "not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        assertThrows(BusinessException.class, () ->
                aiService.parseTodo(USER_ID, SPACE_ID, new ParseTodoRequest("test")));
    }
}