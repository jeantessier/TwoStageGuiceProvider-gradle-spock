package service;

public class ServiceImpl implements Service {
    private String state = "generic";

    public void setupClient1() {
        state = "client1";
    }

    public void setupClient2() {
        state = "client2";
    }

    public String getState() {
        return state;
    }
}
