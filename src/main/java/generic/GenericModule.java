package generic;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import service.Service;
import service.ServiceImpl;

public class GenericModule extends AbstractModule {
    @Provides
    @Named("generic")
    public Service provideService() {
        return new ServiceImpl();
    }
}
