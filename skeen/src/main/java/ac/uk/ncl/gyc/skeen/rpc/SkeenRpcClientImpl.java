package ac.uk.ncl.gyc.skeen.rpc;

import com.alipay.remoting.exception.RemotingException;

import com.alipay.remoting.rpc.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ac.uk.ncl.gyc.skeen.exception.SkeenRemotingException;

public class SkeenRpcClientImpl implements SkeenRpcClient {

    public static Logger logger = LoggerFactory
            .getLogger(SkeenRpcClientImpl.class.getName());

    private final static RpcClient CLIENT = new com.alipay.remoting.rpc.RpcClient();

    static {
        CLIENT.init();
    }


    @Override
    public Response send(Request request) {
        Response result = null;
        try {
            result = (Response) CLIENT.invokeSync(request.getUrl(), request, 200000);
        } catch (RemotingException e) {
            e.printStackTrace();
            logger.info("rpc SkeenRemotingException ");
            throw new SkeenRemotingException();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }
}
