package programm;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class Test {
    private static TransportMapping<UdpAddress> transport;
    private static Snmp snmp;

    public static void main(String[] args) throws IOException {

        startListening();
        System.out.println(getCurrentTemperature());
    }

    private static void startListening() throws IOException {
        if (transport == null || snmp == null) {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();
        }
    }

    private static int getCurrentTemperature() throws IOException {
        String oid = "1.3.6.1.4.1.9.9.13.1.3.1.3.1006"; // > 99
        ResponseEvent responseEvent = get(new OID[]{new OID(oid)});   // нужно, чтобы каждый раз новый вызывался
        try {
            return responseEvent.getResponse().get(0).getVariable().toInt();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    private static ResponseEvent get(OID[] oids) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        try {
            ResponseEvent event = snmp.send(pdu, setParameters(), null);
            return event;
        } catch (MessageException | NullPointerException e) {
            System.out.println("data");
        }
        throw new RuntimeException("проблема с ip");
    }

    private static Target setParameters() {
        String ipAndPort = "0.77.82.7" + "/161";  // 0, >=99, nullpointer в 51 строке
        Address targetAddress = GenericAddress.parse(ipAndPort);
        var target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(1);
        target.setTimeout(200);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

}
