package client1;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import generic.Client;
import service.Service;

public class Client1 extends Client {
    @Inject
    public Client1(@Named("client1") Service service) {
        super(service);
    }
}
