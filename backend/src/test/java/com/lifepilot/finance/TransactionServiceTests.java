package com.lifepilot.finance;

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
import com.lifepilot.finance.dto.CreateTransactionRequest;
import com.lifepilot.finance.dto.TransactionResponse;
import com.lifepilot.finance.dto.UpdateTransactionRequest;
import com.lifepilot.space.HouseholdService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTests {

    @Mock
    private TransactionRecordMapper recordMapper;
    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private TransactionService transactionService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long TX_ID = 100L;

    // --- create ---

    @Test
    void create_success_setsDefaults() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("50.00"), null, null, null, null, "超市", "买菜"
        );

        TransactionResponse response = transactionService.create(USER_ID, SPACE_ID, request);

        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(recordMapper).insert((TransactionRecord) any());
        assertNotNull(response);
    }

    @Test
    void create_withExplicitTypeAndCurrency() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("100"), "income", "USD", 5L,
                LocalDateTime.of(2026, 1, 1, 10, 0), "公司", "工资"
        );

        TransactionResponse response = transactionService.create(USER_ID, SPACE_ID, request);

        verify(recordMapper).insert((TransactionRecord) any());
        assertNotNull(response);
    }

    @Test
    void create_nonMember_throwsException() {
        doThrow(new BusinessException("FORBIDDEN", "Not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("50"), null, null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> transactionService.create(USER_ID, SPACE_ID, request));
        verify(recordMapper, never()).insert((TransactionRecord) any());
    }

    // --- list ---

    @Test
    void list_returnsTransactions() {
        TransactionRecord record = new TransactionRecord();
        record.setId(TX_ID);
        record.setHouseholdId(SPACE_ID);
        record.setType("expense");
        record.setAmount(new BigDecimal("100"));
        record.setOccurredAt(LocalDateTime.now());

        when(recordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(record));

        List<TransactionResponse> result = transactionService.list(USER_ID, SPACE_ID);

        assertEquals(1, result.size());
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
    }

    @Test
    void list_empty_returnsEmptyList() {
        when(recordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<TransactionResponse> result = transactionService.list(USER_ID, SPACE_ID);

        assertTrue(result.isEmpty());
    }

    // --- get ---

    @Test
    void get_found_returnsTransaction() {
        TransactionRecord record = new TransactionRecord();
        record.setId(TX_ID);
        record.setHouseholdId(SPACE_ID);
        record.setType("expense");
        record.setAmount(new BigDecimal("200"));

        when(recordMapper.selectById(TX_ID)).thenReturn(record);

        TransactionResponse result = transactionService.get(USER_ID, SPACE_ID, TX_ID);

        assertNotNull(result);
        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
    }

    @Test
    void get_notFound_throwsException() {
        when(recordMapper.selectById(TX_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> transactionService.get(USER_ID, SPACE_ID, TX_ID));
    }

    @Test
    void get_wrongSpace_throwsException() {
        TransactionRecord record = new TransactionRecord();
        record.setId(TX_ID);
        record.setHouseholdId(999L);

        when(recordMapper.selectById(TX_ID)).thenReturn(record);

        assertThrows(BusinessException.class,
                () -> transactionService.get(USER_ID, SPACE_ID, TX_ID));
    }

    // --- update ---

    @Test
    void update_success_updatesFields() {
        TransactionRecord record = new TransactionRecord();
        record.setId(TX_ID);
        record.setHouseholdId(SPACE_ID);
        record.setType("expense");
        record.setAmount(new BigDecimal("100"));

        when(recordMapper.selectById(TX_ID)).thenReturn(record);

        UpdateTransactionRequest request = new UpdateTransactionRequest(
                new BigDecimal("200"), "income", null, null, "新商户", null
        );

        TransactionResponse result = transactionService.update(USER_ID, SPACE_ID, TX_ID, request);

        assertNotNull(result);
        verify(recordMapper).updateById((TransactionRecord) any());
    }

    @Test
    void update_notFound_throwsException() {
        when(recordMapper.selectById(TX_ID)).thenReturn(null);

        UpdateTransactionRequest request = new UpdateTransactionRequest(
                null, null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> transactionService.update(USER_ID, SPACE_ID, TX_ID, request));
        verify(recordMapper, never()).updateById((TransactionRecord) any());
    }

    // --- delete ---

    @Test
    void delete_success_deletesRecord() {
        TransactionRecord record = new TransactionRecord();
        record.setId(TX_ID);
        record.setHouseholdId(SPACE_ID);

        when(recordMapper.selectById(TX_ID)).thenReturn(record);

        transactionService.delete(USER_ID, SPACE_ID, TX_ID);

        verify(recordMapper).deleteById(TX_ID);
    }

    @Test
    void delete_notFound_throwsException() {
        when(recordMapper.selectById(TX_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> transactionService.delete(USER_ID, SPACE_ID, TX_ID));
        verify(recordMapper, never()).deleteById((Long) any());
    }
}