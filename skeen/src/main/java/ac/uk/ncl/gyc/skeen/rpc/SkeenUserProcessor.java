package ac.uk.ncl.gyc.skeen.rpc;

import ac.uk.ncl.gyc.skeen.exception.SkeenNotSupportException;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AbstractUserProcessor;

/**
 * @author 莫那·鲁道
 */
public abstract class SkeenUserProcessor<T> extends AbstractUserProcessor<T> {

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) {
        throw new SkeenNotSupportException(
                "Raft Server not support handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) ");
    }


    @Override
    public String interest() {
        return Request.class.getName();
    }
}
