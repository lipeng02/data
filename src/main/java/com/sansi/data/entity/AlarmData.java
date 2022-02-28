package com.sansi.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("warninghints")
public class AlarmData {
    //指定自增策略
    @TableId(value = "id",type = IdType.AUTO)
    private long id;
    private String a0_1;
    private int a2_3;
    private String a4_13;
    private int a14_15;
    private int a16_17;
    private int a18_19;
    private Timestamp a20_27;
    private Timestamp a28_35;
    private int a36_39;
    private double a40_43;
    private int a44_45;
    private String a46_57;
    private int a58_59;
    private int a60_61;
    private String bjid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
