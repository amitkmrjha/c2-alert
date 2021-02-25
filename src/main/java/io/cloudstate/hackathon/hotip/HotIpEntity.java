package io.cloudstate.hackathon.hotip;


import com.google.protobuf.Empty;
import com.hackathon.hotip.Hotip;
import com.hackathon.hotip.persistence.Domain;
import io.cloudstate.javasupport.EntityId;
import io.cloudstate.javasupport.eventsourced.*;
import java.util.Optional;

@EventSourcedEntity
public class HotIpEntity {

    private final String entityId;

    private Optional<Domain.HotIpState> hotIpInfoOption = Optional.empty();

    public HotIpEntity(@EntityId String entityId) {
        this.entityId = entityId;
    }

    @Snapshot
    public Domain.HotIpState snapshot() {
        return hotIpInfoOption.orElse(Domain.HotIpState.getDefaultInstance());
    }

    @SnapshotHandler
    public void handleSnapshot(Domain.HotIpState hotIpState) {
        this.hotIpInfoOption = Optional.ofNullable(hotIpState);
    }

    @CommandHandler
    public com.google.protobuf.Empty addHotIp(Hotip.HotIpEvent ipEvent, CommandContext ctx) {
        Domain.HotIpAddedEvent hotIpAdded = Domain.HotIpAddedEvent.newBuilder()
                    .setIp(ipEvent.getIp())
                    .setAppSha256(ipEvent.getAppSha256())
                    .build();
        ctx.emit(hotIpAdded);
        return Empty.getDefaultInstance();
    }

    @EventHandler
    public void hotIpAdded(Domain.HotIpAddedEvent hotIpAdded) {
        if(this.hotIpInfoOption.isPresent()){
            Domain.HotIpState currentState = this.hotIpInfoOption.get();
            if(currentState.containsAppFrequency(hotIpAdded.getAppSha256())){
                int currentCount = currentState.getAppFrequencyCount();
                Domain.HotIpState updatedState = Domain.HotIpState.newBuilder()
                        .setCount(currentState.getCount()+1)
                        .putAppFrequency(hotIpAdded.getAppSha256(),currentCount)
                        .build();
                this.hotIpInfoOption = Optional.ofNullable(updatedState);
            }else{
                Domain.HotIpState updatedState = Domain.HotIpState.newBuilder()
                        .setCount(currentState.getCount()+1)
                        .putAppFrequency(hotIpAdded.getAppSha256(),1)
                        .build();
                this.hotIpInfoOption = Optional.ofNullable(updatedState);
            }
        }else {
            Domain.HotIpState newState =Domain.HotIpState.newBuilder()
                    .setCount(1)
                    .putAppFrequency(hotIpAdded.getAppSha256(),1)
                    .build();
            this.hotIpInfoOption = Optional.ofNullable(newState);
        }

    }

    @CommandHandler
    public Hotip.HotIpInfo getHotIp(Hotip.GetHotIpEvent hotIpEvent) {
        return convert(this.hotIpInfoOption);
    }
    private Hotip.HotIpInfo convert(Optional<Domain.HotIpState> stateOption) {

            if(!stateOption.isPresent()){
                return Hotip.HotIpInfo.getDefaultInstance();
            }else{
                Domain.HotIpState state = this.hotIpInfoOption.get();
                return Hotip.HotIpInfo.newBuilder()
                        .setIp(this.entityId)
                        .setCount(state.getCount())
                        .putAllAppFrequency(state.getAppFrequencyMap())
                        .build();

            }
    }
}
