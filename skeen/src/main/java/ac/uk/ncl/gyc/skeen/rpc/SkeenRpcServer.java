package ac.uk.ncl.gyc.skeen.rpc;

public interface SkeenRpcServer {

    void start();

    void stop();

    Response handlerRequest(Request request) throws InterruptedException;

}
