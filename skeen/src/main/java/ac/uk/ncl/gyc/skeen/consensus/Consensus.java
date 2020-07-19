package ac.uk.ncl.gyc.skeen.consensus;


import ac.uk.ncl.gyc.skeen.entity.InitialTaskRequest;
import ac.uk.ncl.gyc.skeen.entity.InitialTaskResponse;
import ac.uk.ncl.gyc.skeen.entity.LcSendRequest;
import ac.uk.ncl.gyc.skeen.entity.LcSendResponse;

public interface Consensus {

    LcSendResponse sendLogicTime(LcSendRequest lcSendRequest) throws InterruptedException;

    InitialTaskResponse InitialTask(InitialTaskRequest request);
}
