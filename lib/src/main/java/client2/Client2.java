package client2;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import generic.Client;
import service.Service;

public class Client2 extends Client {
    @Inject
    public Client2(@Named("client2") Service service) {
        super(service);
    }
}
