package com.lifepilot.finance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.finance.dto.CreateTransactionRequest;
import com.lifepilot.finance.dto.TransactionResponse;
import com.lifepilot.finance.dto.UpdateTransactionRequest;
import com.lifepilot.space.HouseholdService;

@Service
public class TransactionService {

    private final TransactionRecordMapper recordMapper;
    private final HouseholdService householdService;

    public TransactionService(TransactionRecordMapper recordMapper, HouseholdService householdService) {
        this.recordMapper = recordMapper;
        this.householdService = householdService;
    }

    @Transactional
    public TransactionResponse create(Long userId, Long spaceId, CreateTransactionRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        TransactionRecord record = new TransactionRecord();
        record.setHouseholdId(spaceId);
        record.setUserId(userId);
        record.setAmount(request.amount());
        record.setType(request.type() != null ? request.type() : "expense");
        record.setCurrency(request.currency() != null ? request.currency() : "CNY");
        record.setCategoryId(request.categoryId());
        record.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : LocalDateTime.now());
        record.setMerchant(request.merchant());
        record.setNote(request.note());
        record.setSource("manual");
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        recordMapper.insert(record);
        return TransactionResponse.from(record);
    }

    public List<TransactionResponse> list(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<TransactionRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<TransactionRecord>()
                        .eq(TransactionRecord::getHouseholdId, spaceId)
                        .orderByDesc(TransactionRecord::getOccurredAt)
        );

        return records.stream().map(TransactionResponse::from).collect(Collectors.toList());
    }

    public TransactionResponse get(Long userId, Long spaceId, Long transactionId) {
        householdService.requireSpaceMembership(userId, spaceId);

        TransactionRecord record = recordMapper.selectById(transactionId);
        if (record == null || !record.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Transaction not found");
        }
        return TransactionResponse.from(record);
    }

    @Transactional
    public TransactionResponse update(Long userId, Long spaceId, Long transactionId, UpdateTransactionRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        TransactionRecord record = recordMapper.selectById(transactionId);
        if (record == null || !record.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Transaction not found");
        }

        if (request.amount() != null) record.setAmount(request.amount());
        if (request.type() != null) record.setType(request.type());
        if (request.categoryId() != null) record.setCategoryId(request.categoryId());
        if (request.occurredAt() != null) record.setOccurredAt(request.occurredAt());
        if (request.merchant() != null) record.setMerchant(request.merchant());
        if (request.note() != null) record.setNote(request.note());
        record.setUpdatedAt(LocalDateTime.now());

        recordMapper.updateById(record);
        return TransactionResponse.from(record);
    }

    @Transactional
    public void delete(Long userId, Long spaceId, Long transactionId) {
        householdService.requireSpaceMembership(userId, spaceId);

        TransactionRecord record = recordMapper.selectById(transactionId);
        if (record == null || !record.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Transaction not found");
        }

        recordMapper.deleteById(transactionId);
    }
}