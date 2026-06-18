package com.lifepilot.todo;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface TodoTaskMapper extends BaseMapper<TodoTask> {
}