package io.cloudstate.hackathon.hostip;
import com.hackathon.hostip.Hostip;
import io.cloudstate.javasupport.*;
import static java.util.Collections.singletonMap;

public final class Main {

  public static final void main(String[] args) throws Exception {
    new CloudState()
            .registerEventSourcedEntity(
                    HostIpEntity.class,
                    Hostip.getDescriptor().findServiceByName("HostIp"),
                    com.hackathon.hostip.persistence.Domain.getDescriptor())
            .start()
            .toCompletableFuture()
            .get();

  }
}
