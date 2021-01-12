package client1;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import service.Service;

public class Client1Module extends AbstractModule {
    @Provides
    @Named("client1")
    public Service provideService(@Named("generic") Service service) {
        service.setupClient1();
        return service;
    }
}
