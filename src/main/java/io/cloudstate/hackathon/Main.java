package io.cloudstate.hackathon;
import com.hackathon.hostip.Hostip;
import com.hackathon.hotip.Hotip;
import io.cloudstate.hackathon.hostip.HostIpEntity;
import io.cloudstate.hackathon.hotip.HotIpEntity;
import io.cloudstate.javasupport.*;
import static java.util.Collections.singletonMap;

public final class Main {

  public static final void main(String[] args) throws Exception {
    new CloudState()
            .registerEventSourcedEntity(
                    HostIpEntity.class,
                    Hostip.getDescriptor().findServiceByName("HostIp"),
                    com.hackathon.hostip.persistence.Domain.getDescriptor())
            .registerEventSourcedEntity(
                    HotIpEntity.class,
                    Hotip.getDescriptor().findServiceByName("HotIp"),
                    com.hackathon.hotip.persistence.Domain.getDescriptor())
            .start()
            .toCompletableFuture()
            .get();

  }
}
