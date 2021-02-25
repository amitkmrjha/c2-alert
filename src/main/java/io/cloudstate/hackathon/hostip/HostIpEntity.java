package io.cloudstate.hackathon.hostip;
import com.google.protobuf.Empty;
import com.hackathon.hostip.persistence.Domain;
import io.cloudstate.javasupport.EntityId;
import io.cloudstate.javasupport.eventsourced.*;
import com.hackathon.hostip.Hostip;

import java.util.*;
import java.util.stream.Collectors;

@EventSourcedEntity
public class HostIpEntity {

    private final String entityId;

    private  Optional<Domain.IpEventState> hostIpInfoOption = Optional.empty();

    public HostIpEntity(@EntityId String entityId) {
        this.entityId = entityId;
    }

    @Snapshot
    public Domain.IpEventState snapshot() {
        return hostIpInfoOption.orElse(Domain.IpEventState.getDefaultInstance());
    }

    @SnapshotHandler
    public void handleSnapshot(Domain.IpEventState ipEventState) {
        this.hostIpInfoOption = Optional.ofNullable(ipEventState);
    }

    @CommandHandler
    public com.google.protobuf.Empty addHostIp(Hostip.IpEvent ipEvent,CommandContext ctx) {
        Long ip = ipEvent.getIp();
        if(isGoodIp(ip)){
            Domain.GoodIpAdded goodIpAdded =   Domain.GoodIpAdded.newBuilder()
            .setIp(ip)
            .build();
            ctx.emit(goodIpAdded);
        }else {
            Domain.BadIpAdded badIpAdded =   Domain.BadIpAdded.newBuilder()
                    .setIp(ip)
                    .build();
            ctx.emit(badIpAdded);
        }
        return Empty.getDefaultInstance();
    }

    @EventHandler
    public void goodIpAdded(Domain.GoodIpAdded goodIpAdded) {

        if(this.hostIpInfoOption.isPresent()){
            Domain.IpEventState currentState = this.hostIpInfoOption.get();
            Set<Long> goodIpList = currentState.getGoodIpsList().stream().collect(Collectors.toSet());
            goodIpList.add(goodIpAdded.getIp());
            Domain.IpEventState updatedState = Domain.IpEventState.newBuilder()
                    .setCount(currentState.getCount()+1)
                    .addAllGoodIps(goodIpList)
                    .addAllBadIps(currentState.getBadIpsList())
                    .build();
            this.hostIpInfoOption = Optional.ofNullable(updatedState);
        }else {
            Domain.IpEventState newState = Domain.IpEventState.newBuilder()
                    .setCount(1)
                    .addGoodIps(goodIpAdded.getIp())
                    .build();
            this.hostIpInfoOption = Optional.ofNullable(newState);
        }
    }

    @EventHandler
    public void badIpAdded(Domain.BadIpAdded badIpAdded) {
        if(this.hostIpInfoOption.isPresent()){
            Domain.IpEventState currentState = this.hostIpInfoOption.get();
            Set<Long> badIpList = currentState.getBadIpsList().stream().collect(Collectors.toSet());
            badIpList.add(badIpAdded.getIp());
            Domain.IpEventState updatedState = Domain.IpEventState.newBuilder()
                    .setCount(currentState.getCount()+1)
                    .addAllGoodIps(currentState.getGoodIpsList())
                    .addAllBadIps(badIpList)
                    .build();
            this.hostIpInfoOption = Optional.ofNullable(updatedState);
        }else {
            Domain.IpEventState newState = Domain.IpEventState.newBuilder()
                    .setCount(1)
                    .addBadIps(badIpAdded.getIp())
                    .build();
            this.hostIpInfoOption = Optional.ofNullable(newState);
        }
    }

    @CommandHandler
    public com.google.protobuf.Empty removeHostIp(Hostip.RemoveHostEvent removeHostEvent,CommandContext ctx) {

        if(!this.hostIpInfoOption.isPresent()){
            ctx.fail("Cannot archive host ip "+removeHostEvent.getAppSha256()+" It is un-initialized or archived state.  ");
        }
        ctx.emit(Domain.HostArchived.newBuilder().build());
        return Empty.getDefaultInstance();
    }

    @EventHandler
    public void hostArchived(Domain.HostArchived hostArchived) {
        this.hostIpInfoOption = Optional.empty();
    }

    @CommandHandler
    public Hostip.HostIpInfo getHostIp(Hostip.GetHostEvent getHostEvent) {

        return convert(getState(this.hostIpInfoOption));
    }

    private Domain.StateInfo getState(Optional<Domain.IpEventState> stateOption) {
        if(!stateOption.isPresent()){
            return Domain.StateInfo.getDefaultInstance();
        }else{
            Domain.IpEventState state = this.hostIpInfoOption.get();
            return Domain.StateInfo.newBuilder()
                    .setAppSha256(this.entityId)
                    .setCount(state.getCount())
                    .addAllGoodIps(state.getGoodIpsList())
                    .addAllBadIps(state.getBadIpsList())
                    .build();

        }
    }
    private Hostip.HostIpInfo convert(Domain.StateInfo stateInfo) {
        return Hostip.HostIpInfo.newBuilder()
                .setAppSha256(stateInfo.getAppSha256())
                .setCount(stateInfo.getCount())
                .addAllGoodIps(stateInfo.getGoodIpsList())
                .addAllBadIps(stateInfo.getBadIpsList())
                .build();
    }

    private boolean isGoodIp(Long ip) {
        if((ip%2)==0) {
            return true;
        } else return false;
    }

}