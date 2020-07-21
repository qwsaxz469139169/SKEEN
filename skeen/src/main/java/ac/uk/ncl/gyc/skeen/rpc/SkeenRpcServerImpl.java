package ac.uk.ncl.gyc.skeen.rpc;

import ac.uk.ncl.gyc.skeen.entity.InitialTaskRequest;
import ac.uk.ncl.gyc.skeen.entity.LcSendRequest;
import ac.uk.ncl.gyc.skeen.node.NodeImpl;

import com.alipay.remoting.BizContext;


import com.alipay.remoting.rpc.RpcServer;
import ac.uk.ncl.gyc.skeen.client.ClientRequest;


@SuppressWarnings("unchecked")
public class SkeenRpcServerImpl implements SkeenRpcServer {

    private volatile boolean flag;

    private NodeImpl node;

    private RpcServer rpcServer;

    public SkeenRpcServerImpl(int port, NodeImpl node) {

        if (flag) {
            return;
        }
        synchronized (this) {
            if (flag) {
                return;
            }

            rpcServer = new com.alipay.remoting.rpc.RpcServer(port, false, false);

            rpcServer.registerUserProcessor(new SkeenUserProcessor<Request>() {

                @Override
                public Object handleRequest(BizContext bizCtx, Request request) throws Exception {
                    return handlerRequest(request);
                }
            });

            this.node = node;
            flag = true;
        }

    }

    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public void stop() {
        rpcServer.stop();
    }

    @Override
    public Response handlerRequest(Request request) throws InterruptedException {
        long receiveTime = System.currentTimeMillis();
        System.out.println("handlerRequest method param: "+ request.getObj());
        if (request.getCmd() == Request.REQ_SEND_LC) {
            return new Response(node.handlerSendLcRequest((LcSendRequest) request.getObj()));
        } else if (request.getCmd() == Request.REQ_CLIENT) {
            return new Response(node.handlerClientRequest((ClientRequest) request.getObj(),receiveTime));
        }else if (request.getCmd() == Request.REQ_INI_TASK) {
            return new Response(node.handlerInitialTask((InitialTaskRequest) request.getObj()));
        }
        return null;
    }


}
