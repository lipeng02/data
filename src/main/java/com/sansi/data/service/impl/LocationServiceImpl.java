package com.sansi.data.service.impl;

import com.sansi.data.dao.LocationMapper;
import com.sansi.data.entity.Location;
import com.sansi.data.service.ILocationService;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl extends BaseServiceImpl<LocationMapper, Location> implements ILocationService {
}
