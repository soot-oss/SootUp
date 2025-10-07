package net.service.impl;

import myservice.IService;

public class ServiceImpl implements IService {
    public ServiceImpl() { }

    @Override
    public String getName() {
        return ServiceImpl.class.getName();
    }
}