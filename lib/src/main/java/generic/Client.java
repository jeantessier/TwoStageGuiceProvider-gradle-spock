package generic;

import service.Service;

public class Client {
    private final Service service;

    public Client(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }
}
