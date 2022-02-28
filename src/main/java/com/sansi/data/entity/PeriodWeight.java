package com.sansi.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("weight")
public class PeriodWeight {
    private String topic;
    private float weight;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp updatedAt;
}
