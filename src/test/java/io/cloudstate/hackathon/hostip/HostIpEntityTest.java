package io.cloudstate.hackathon.hostip;

import com.hackathon.hostip.Hostip;
import com.hackathon.hostip.persistence.Domain;
import com.hackathon.hotip.Hotip;
import io.cloudstate.javasupport.ServiceCallRef;
import io.cloudstate.javasupport.eventsourced.CommandContext;
import io.cloudstate.javasupport.eventsourced.EventSourcedEntityCreationContext;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


public class HostIpEntityTest {

    String accessToken = "accessToken";
    String room = "person-cave";

    @Test
    public void createHostIpTest() {
        CommandContext context = Mockito.mock(CommandContext.class);
        ServiceCallRef<Hotip.HotIpEvent> hotIpEventRef = Mockito.mock(ServiceCallRef.class);

        String hostIpId = "customerId1";
        Hostip.IpEvent ipEvent1 = randomIpEvent(hostIpId);
        Hostip.IpEvent ipEvent2 = randomIpEvent(hostIpId);
        Hostip.IpEvent ipEvent3 = randomIpEvent(hostIpId);




//"com.hackathon.hotip.HotIpService", "AddHotIp", Hotip.HotIpEvent.class
        context.serviceCallFactory()
                .lookup(
                        "com.hackathon.hotip.HotIpService", "AddHotIp", Hotip.HotIpEvent.class);

        //HostIpEntity hostIp = new HostIpEntity(hostIpId);
        HostIpEntity hostIp = new HostIpEntity(context);

        hostIp.addHostIp(ipEvent1, context);
        emitAddContextEvent(context,hostIp,ipEvent1);

        hostIp.addHostIp(ipEvent2, context);
        emitAddContextEvent(context,hostIp,ipEvent2);

        hostIp.addHostIp(ipEvent3, context);
        emitAddContextEvent(context,hostIp,ipEvent3);

        Hostip.HostIpInfo hostIpInfo = hostIp.getHostIp(getHostEvent(hostIpId));
        //System.out.println("hostIpInfo app_sha256 "+hostIpInfo.getAppSha256());
        //System.out.println("hostIpInfo count "+hostIpInfo.getCount());
        //System.out.println("hostIpInfo good ips "+hostIpInfo.getGoodIpsList().stream().collect(Collectors.toList()));
        //System.out.println("hostIpInfo bad ips "+hostIpInfo.getBadIpsList().stream().collect(Collectors.toList()));
        assertEquals(3,hostIpInfo.getCount());
    }
    @Test
    public void removeItemTest() {

        CommandContext context = Mockito.mock(CommandContext.class);

        String hostIpId = "customerId1";
        Hostip.IpEvent ipEvent1 = randomIpEvent(hostIpId);
        Hostip.IpEvent ipEvent2 = randomIpEvent(hostIpId);
        Hostip.IpEvent ipEvent3 = randomIpEvent(hostIpId);
        HostIpEntity hostIp = new HostIpEntity(context);

        hostIp.addHostIp(ipEvent1, context);
        emitAddContextEvent(context,hostIp,ipEvent1);

        hostIp.addHostIp(ipEvent2, context);
        emitAddContextEvent(context,hostIp,ipEvent2);

        hostIp.addHostIp(ipEvent3, context);
        emitAddContextEvent(context,hostIp,ipEvent3);

        hostIp.removeHostIp(getRemoveHostEvent(hostIpId),context);
        emitRemoveContextEvent(context,hostIp);

        Hostip.HostIpInfo hostIpInfo = hostIp.getHostIp(getHostEvent(hostIpId));
        //System.out.println("hostIpInfo app_sha256 "+hostIpInfo.getAppSha256());
        //System.out.println("hostIpInfo count "+hostIpInfo.getCount());
        //System.out.println("hostIpInfo good ips "+hostIpInfo.getGoodIpsList().stream().collect(Collectors.toList()));
        //System.out.println("hostIpInfo bad ips "+hostIpInfo.getBadIpsList().stream().collect(Collectors.toList()));
        assertEquals(0,hostIpInfo.getCount());

    }
    private Hostip.IpEvent randomIpEvent(String app_sha256){
        return Hostip.IpEvent.newBuilder()
                .setAppSha256(app_sha256)
                .setIp(new Random().nextLong())
                .build();
    }
    private Hostip.GetHostEvent getHostEvent(String app_sha256){
        return Hostip.GetHostEvent.newBuilder()
                .setAppSha256(app_sha256)
                .build();
    }
    private Hostip.RemoveHostEvent getRemoveHostEvent(String app_sha256){
        return Hostip.RemoveHostEvent.newBuilder()
                .setAppSha256(app_sha256)
                .build();
    }

    private void emitAddContextEvent(CommandContext ctx, HostIpEntity hostIp, Hostip.IpEvent ipEvent){
        if(isGoodIp(ipEvent.getIp())){
            Domain.GoodIpAdded goodIpAdded = getGoodIpAdded(ipEvent);
            Mockito.verify(ctx).emit(goodIpAdded);
            hostIp.goodIpAdded(goodIpAdded);
        }else{
            Domain.BadIpAdded badIpAdded = getBadIpAdded(ipEvent);
            Mockito.verify(ctx).emit(badIpAdded);
            hostIp.badIpAdded(badIpAdded);
        }
    }

    private void emitRemoveContextEvent(CommandContext ctx, HostIpEntity hostIp){
        Domain.HostArchived hostArchived = Domain.HostArchived.getDefaultInstance();
        Mockito.verify(ctx).emit(hostArchived);
        hostIp.hostArchived(hostArchived);
    }

    private boolean isGoodIp(Long ip) {
        if((ip%2)==0) {
            return true;
        } else return false;
    }

    private Domain.GoodIpAdded getGoodIpAdded(Hostip.IpEvent ipEvent){
        return Domain.GoodIpAdded.newBuilder()
                .setAppSha256(ipEvent.getAppSha256())
                .setIp(ipEvent.getIp())
                .build();
    }
    private Domain.BadIpAdded getBadIpAdded(Hostip.IpEvent ipEvent){
        return Domain.BadIpAdded.newBuilder()
                .setAppSha256(ipEvent.getAppSha256())
                .setIp(ipEvent.getIp())
                .build();
    }
}
