package com.neighborhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community")
public class Community {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Double longitude;
    private Double latitude;
    private String address;
    private LocalDateTime createTime;
}