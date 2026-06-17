package com.lifepilot.ai;

import org.springframework.stereotype.Service;

import com.lifepilot.ai.dto.ParseTransactionRequest;
import com.lifepilot.ai.dto.TransactionDraftResponse;
import com.lifepilot.space.HouseholdService;

@Service
public class AiService {

    private final AiProvider aiProvider;
    private final HouseholdService householdService;

    public AiService(AiProvider aiProvider, HouseholdService householdService) {
        this.aiProvider = aiProvider;
        this.householdService = householdService;
    }

    public TransactionDraftResponse parseTransaction(Long userId, Long spaceId, ParseTransactionRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);
        TransactionDraftResponse draft = aiProvider.parseTransaction(request.text());
        if (draft == null) {
            return new TransactionDraftResponse(
                    null, null, "CNY", null, null, null,
                    null, true, request.text(), "无法解析输入文本，请尝试重新描述。"
            );
        }
        return draft;
    }
}