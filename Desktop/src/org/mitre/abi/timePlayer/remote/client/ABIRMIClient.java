package org.mitre.abi.timePlayer.remote.client;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ABIRMIClient {

    private static Logger log = LoggerFactory.getLogger(ABIRMIClient.class);

    private ABIRMIClient() {
    }

    public static void writeToMap() {

        try {
            Registry registry = LocateRegistry.getRegistry(InetAddress.getByName("localhost").getHostAddress(), 1099);
            AnalystInterface stub = (AnalystInterface) registry.lookup("AnalystInterface");
            
            List<SimpleTimeDot> timeDots = new ArrayList<SimpleTimeDot>();
            
            for (int i = 0; i < 100; i++) {
                
                SimpleTimeDot dot = new SimpleTimeDot(42 + i / 30.0, -71 + i / 30.0, -1, -1);
                timeDots.add(dot);
                
                // add dots with time
                SimpleTimeDot dot2 = new SimpleTimeDot(44 + i / 30.0, -80 + i / 30.0, 12308405 + i * 1000, 12309405 + i * 1000);
                timeDots.add(dot2);

            }
            
            stub.addSliceToTDC("FooBar", timeDots);
            System.out.println("Slice should have been added to the TDC");
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void writeToMap(String name, List<SimpleTimeDot> timeDots) {
        try {
            Registry registry = LocateRegistry.getRegistry(InetAddress.getByName("localhost").getHostAddress(), 1099);
            AnalystInterface stub = (AnalystInterface) registry.lookup("AnalystInterface");
            
            stub.addSliceToTDC(name, timeDots);
            System.out.println("Slice should have been added to the TDC");
        } catch (Exception e) {
            log.error("error writing data to ABI map", e);
        }
    }
}
