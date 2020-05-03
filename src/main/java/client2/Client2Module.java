package client2;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import service.Service;

public class Client2Module extends AbstractModule {
    @Provides
    @Named("client2")
    public Service provideService(@Named("generic") Service service) {
        service.setupClient2();
        return service;
    }
}
