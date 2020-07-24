package ac.uk.ncl.gyc.skeen;

import java.util.Arrays;

import ac.uk.ncl.gyc.skeen.node.Node;
import ac.uk.ncl.gyc.skeen.node.NodesConfigration;
import ac.uk.ncl.gyc.skeen.node.NodeImpl;

/**
 * -DserverPort=8775
 * -DserverPort=8776
 * -DserverPort=8777
 * -DserverPort=8778
 * -DserverPort=8779
 */
public class SkeenBootStrap {

    public static void main(String[] args) throws Throwable {
        main0(args);
    }

    public static void main0(String[] args) throws Throwable {
        //   String[] nodesAddress = {"localhost:8775","localhost:8776","localhost:8777"};
      String[] nodesAddress = {"100.70.49.128:8775", "100.70.49.85:8776", "100.70.48.226:8777"};

        NodesConfigration config = new NodesConfigration();

        // 自身节点
        config.setSelfPort(Integer.valueOf(System.getProperty("serverPort")));
//        config.setSelfPort(Integer.valueOf(args[0]));

        // 其他节点地址
        config.setPeerAddrs(Arrays.asList(nodesAddress));

        Node node = NodeImpl.getInstance();
        node.setConfig(config);

        node.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                node.destroy();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }));

    }

}
