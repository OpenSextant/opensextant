package org.mitre.abi.timePlayer.remote.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AnalystInterface extends Remote {

    public void addSliceToTDC(String name, List<SimpleTimeDot> dots) throws RemoteException;
    
}
