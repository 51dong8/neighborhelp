package com.neighborhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighborhelp.entity.Request;
import org.apache.ibatis.annotations.Mapper;

/**
 * 需求Mapper
 */
@Mapper
public interface RequestMapper extends BaseMapper<Request> {
}

