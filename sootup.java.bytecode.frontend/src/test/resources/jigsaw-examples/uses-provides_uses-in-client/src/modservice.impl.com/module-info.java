module modservice.impl.com {
    requires modservicedefinition;
    provides myservice.IService with com.service.impl.ServiceImpl;
}
