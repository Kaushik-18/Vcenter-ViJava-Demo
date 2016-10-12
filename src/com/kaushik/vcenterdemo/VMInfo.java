package com.kaushik.vcenterdemo;


import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

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
            } else {
                System.out.println("invalid vm name");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void toggleVMState(ServiceInstance instance, String vmName, boolean powerVM) {
        Folder rootFolder = instance.getRootFolder();
        try {
            ManagedEntity entity = new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);
            if (entity != null) {
                VirtualMachine machine = (VirtualMachine) entity;
                if (powerVM) {
                    Task onTask = machine.powerOnVM_Task(null);
                    printPowerOutput(onTask, "power on VM : ");

                } else {
                    Task offTask = machine.powerOffVM_Task();
                    printPowerOutput(offTask, "power off VM: ");
                }
            } else {
                System.out.println("invalid vm name");
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void printPowerOutput(Task task, String title) {
        try {
            task.waitForTask();
            TaskInfo info = task.getTaskInfo();
            System.out.println("Name = " + info.entityName);
            String message = "status = success,";
            if (info.getState() == TaskInfoState.error) {
                message = "status = " + info.getError().localizedMessage + ",";
            }
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
            String completionTime = format.format(info.getCompleteTime().getTime());

            System.out.println(title + message + "completion time = " + completionTime);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    static void shutDownVM(ServiceInstance instance, String vmName) {
        Folder rootFolder = instance.getRootFolder();
        try {
            ManagedEntity entity = new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);
            if (entity != null) {
                VirtualMachine machine = (VirtualMachine) entity;
                machine.shutdownGuest();

                Timer stopTime = new Timer();
                stopTime.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Graceful shutdown failed now trying hard power off");
                        toggleVMState(instance, vmName, false);
                    }
                }, 3 * 60 * 1000);

                do {
                    Thread.sleep(2000);
                }
                while(machine.getRuntime().getPowerState() == VirtualMachinePowerState.poweredOn);

                stopTime.cancel();;


            } else {
                System.out.println("invalid vm name");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Graceful shutdown failed now trying hard power off");
            toggleVMState(instance, vmName, false);
        }
    }

    private static void delayShutDown() {

    }


}
