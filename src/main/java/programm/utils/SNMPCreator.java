package programm.utils;

import javafx.stage.Stage;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import programm.controllers.MainController;

import java.io.IOException;
import java.time.Duration;

import static programm.Constants.*;

public class SNMPCreator {
    private static TransportMapping<UdpAddress> transport;
    private static Snmp snmp;
    private static String oid;
    private static Timer connectionTimer;
    private static ResponseEvent responseEvent;

    public static void startListening() throws IOException {
        if (transport == null || snmp == null) {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();
        }
    }

    public static int getCurrentTemperature() throws IOException {
        oid = USER_PREFS.get(OID, "1.3.6.1.4.1.9.9.13.1.3.1.3.1006");
        responseEvent = get(new OID[]{new OID(oid)});   // нужно, чтобы каждый раз новый вызывался
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
        ResponseEvent event = snmp.send(pdu, setParameters(), null);
        return event;
    }

    private static Target setParameters() {
        String ipAndPort = "udp:" + USER_PREFS.get(IP, "10.177.82.7") + "/161";
        Address targetAddress = GenericAddress.parse(ipAndPort);
        var target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(1);
        target.setTimeout(200);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    public static void checkConnection(Timer timer) {
        try {
            responseEvent.getResponse().get(0).getVariable();
        } catch (NullPointerException e1) {
            if (timer != null) {
                timer.stop();
            }

            connectionTimer = FxTimer.runPeriodically(Duration.ofSeconds(10), () -> {
                try {
                    oid = USER_PREFS.get(OID, "1.3.6.1.4.1.9.9.13.1.3.1.3.1006");
                    ResponseEvent ev = get(new OID[]{new OID(oid)});
                    ev.getResponse().get(0).getVariable();
                    {       // блок выполнится, если не будет NullPointerException в предыдущей строке
                        connectionTimer.stop();
                        Stage waitConnection = ((Stage) MainController.getConnectionDialog().getDialogPane().getScene().getWindow());
                        waitConnection.hide();
                        timer.restart();
                    }
                } catch (NullPointerException | IOException e2) {
                    MainController.showWaitConnection();
                }
            });
        }
    }
}