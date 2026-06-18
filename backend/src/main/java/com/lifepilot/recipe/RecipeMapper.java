package com.lifepilot.recipe;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {
}