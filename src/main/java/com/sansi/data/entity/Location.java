package com.sansi.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("location")
public class Location {
    private String id;
    private double lng;
    private double lat;
    private Timestamp updatedAt;
}
