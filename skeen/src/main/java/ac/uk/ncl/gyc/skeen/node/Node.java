package ac.uk.ncl.gyc.skeen.node;

import ac.uk.ncl.gyc.skeen.entity.LcSendRequest;
import ac.uk.ncl.gyc.skeen.entity.LcSendResponse;

import ac.uk.ncl.gyc.skeen.client.ClientResponse;
import ac.uk.ncl.gyc.skeen.client.ClientRequest;

public interface Node<T> extends LifeCycle{

    /**
     * 设置配置文件.
     *
     * @param config
     */
    void setConfig(NodesConfigration config);

    /**
     * 处理客户端请求.
     *
     * @param request
     * @return
     */
    ClientResponse handlerClientRequest(ClientRequest request);


    LcSendResponse handlerSendLcRequest(LcSendRequest request);

}
