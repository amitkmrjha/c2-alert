package io.cloudstate.hackathon.hotip;

import com.hackathon.hotip.Hotip;
import com.hackathon.hotip.persistence.Domain;

import com.hackathon.hotip.Hotip;
import io.cloudstate.hackathon.hotip.HotIpEntity;
import io.cloudstate.javasupport.eventsourced.CommandContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class HotIpEntityTest {

    @Test
    public void createHotIpTest() {

        CommandContext context = Mockito.mock(CommandContext.class);
        String hotIpId = String.valueOf(12345);
        Hotip.HotIpEvent ipEvent1 = randomIpEvent(hotIpId);
        Hotip.HotIpEvent ipEvent2 = randomIpEvent(hotIpId);
        Hotip.HotIpEvent ipEvent3 = randomIpEvent(hotIpId);

        HotIpEntity hotIp = new HotIpEntity(hotIpId);

        hotIp.addHotIp(ipEvent1, context);
        emitAddContextEvent(context,hotIp,ipEvent1);

        hotIp.addHotIp(ipEvent2, context);
        emitAddContextEvent(context,hotIp,ipEvent2);

        hotIp.addHotIp(ipEvent3, context);
        emitAddContextEvent(context,hotIp,ipEvent3);

        Hotip.HotIpInfo hotIpInfo = hotIp.getHotIp(getHotIpEvent(hotIpId));
        //System.out.println("hostIpInfo app_sha256 "+hostIpInfo.getAppSha256());
        //System.out.println("hostIpInfo count "+hostIpInfo.getCount());
        //System.out.println("hostIpInfo good ips "+hostIpInfo.getGoodIpsList().stream().collect(Collectors.toList()));
        //System.out.println("hostIpInfo bad ips "+hostIpInfo.getBadIpsList().stream().collect(Collectors.toList()));
        assertEquals(3,hotIpInfo.getCount());
    }

    private Hotip.HotIpEvent randomIpEvent(String ipId){
        return Hotip.HotIpEvent.newBuilder()
                .setIp(ipId)
                .setAppSha256(String.valueOf(new Random().nextLong()))
                .build();
    }
    private Hotip.GetHotIpEvent getHotIpEvent(String ipId){
        return Hotip.GetHotIpEvent.newBuilder()
                .setIp(ipId)
                .build();
    }

    private void emitAddContextEvent(CommandContext ctx, HotIpEntity hotIp, Hotip.HotIpEvent ipEvent){
             Domain.HotIpAddedEvent hotIpAddedEvent = Domain.HotIpAddedEvent.newBuilder()
                     .setIp(ipEvent.getIp())
                     .setAppSha256(ipEvent.getAppSha256())
                     .build();
            Mockito.verify(ctx).emit(hotIpAddedEvent);
            hotIp.hotIpAdded(hotIpAddedEvent);

    }
}
