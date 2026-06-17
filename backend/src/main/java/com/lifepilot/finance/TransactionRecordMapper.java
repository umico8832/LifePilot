package com.lifepilot.finance;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface TransactionRecordMapper extends BaseMapper<TransactionRecord> {
}