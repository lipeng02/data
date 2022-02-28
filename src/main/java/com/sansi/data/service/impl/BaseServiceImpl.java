package com.sansi.data.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sansi.data.service.BaseService;

/**
 * 通用ServiceImpl
 * @param <M>
 * @param <T>
 */
public class BaseServiceImpl<M extends BaseMapper<T>,T> extends ServiceImpl<M,T> implements BaseService<T> {
}
