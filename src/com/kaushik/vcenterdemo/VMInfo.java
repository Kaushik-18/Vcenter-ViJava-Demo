package com.kaushik.vcenterdemo;

import com.vmware.vim25.CustomizationSysprepRebootOption;
import com.vmware.vim25.mo.*;

import java.rmi.RemoteException;

/**
 * Created by kaushik on 11/10/16.
 */
public class VMInfo {


    public static void getAllVms(ServiceInstance instance) {
        Folder rootFolder = instance.getRootFolder();
        try {
            ManagedEntity[] entityList = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
            int i = 0;
            for (ManagedEntity entity : entityList) {
                VirtualMachine machine = (VirtualMachine) entity;
                String vmName = machine.getName();
                System.out.println("vm[" + i + "] : Name = " + vmName);
                i++;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void getVMInfo(ServiceInstance instance, String vmName) {
        Folder rootFolder = instance.getRootFolder();
        try {
            ManagedEntity entity = new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);
            if (entity != null) {
                VirtualMachine machine = (VirtualMachine) entity;
                System.out.println("Name = " + machine.getName());
                System.out.println("Guest full name = " + machine.getGuest().getGuestFullName());
                System.out.println("Guest state = " + machine.getGuest().getGuestState());
                System.out.println("IP address = " + machine.getGuest().getIpAddress());
                System.out.println("Tool running status = " + machine.getGuest().getToolsRunningStatus());
                System.out.println("Power state = " + machine.getRuntime().getPowerState().name());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
