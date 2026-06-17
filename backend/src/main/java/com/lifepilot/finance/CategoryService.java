package com.lifepilot.finance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.finance.dto.CategoryResponse;
import com.lifepilot.finance.dto.CreateCategoryRequest;
import com.lifepilot.space.HouseholdService;

@Service
public class CategoryService {

    private final TransactionCategoryMapper categoryMapper;
    private final HouseholdService householdService;

    public CategoryService(TransactionCategoryMapper categoryMapper, HouseholdService householdService) {
        this.categoryMapper = categoryMapper;
        this.householdService = householdService;
    }

    @Transactional
    public CategoryResponse create(Long userId, Long spaceId, CreateCategoryRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        TransactionCategory category = new TransactionCategory();
        category.setHouseholdId(spaceId);
        category.setName(request.name());
        category.setType(request.type() != null ? request.type() : "expense");
        category.setIcon(request.icon());
        category.setColor(request.color());
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        categoryMapper.insert(category);
        return CategoryResponse.from(category);
    }

    public List<CategoryResponse> list(Long userId, Long spaceId) {
        householdService.requireSpaceMembership(userId, spaceId);

        List<TransactionCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<TransactionCategory>()
                        .eq(TransactionCategory::getHouseholdId, spaceId)
                        .orderByAsc(TransactionCategory::getType)
                        .orderByAsc(TransactionCategory::getName)
        );

        return categories.stream().map(CategoryResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long userId, Long spaceId, Long categoryId) {
        householdService.requireSpaceMembership(userId, spaceId);

        TransactionCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Category not found");
        }

        categoryMapper.deleteById(categoryId);
    }
}