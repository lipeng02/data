package com.sansi.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("news")
public class AlarmEarly {
    //指定自增策略
    @TableId(value = "id",type = IdType.AUTO)
    private long id;
    private String equipmentId;
    private int bjType;
    private String bjInfo;
}
