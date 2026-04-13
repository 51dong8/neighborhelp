package com.neighborhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neighborhelp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    User selectByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email} LIMIT 1")
    User selectByEmail(String email);
}
