module modservice.impl.net {
    requires modservicedefinition;
    provides myservice.IService with net.service.impl.ServiceImpl;
}
