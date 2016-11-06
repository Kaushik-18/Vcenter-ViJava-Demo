package com.kaushik.vcenterdemo;

import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.*;

import java.io.Console;
import java.net.MalformedURLException;

import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) {
        System.out.println("CMPE 281 HW2 from Kaushik Shingne");

        //Login to server and retrieve service instance
        ServiceInstance currentService = loginToVcenter(args);
        if (currentService != null) {
            Scanner enter = new Scanner(System.in);

            while (true) {
                System.out.print("kaushik-924 >");
                String input = enter.nextLine();
                if ("exit".equalsIgnoreCase(input)) {

                    System.exit(0);

                } else if ("help".equalsIgnoreCase(input)) {

                    printHelpPage();

                } else if ("host".equalsIgnoreCase(input)) {

                    displayAllHosts(currentService);

                } else if ("vm".equalsIgnoreCase(input)) {

                    getAllVms(currentService);

                } else if (input.trim().startsWith("host") || input.trim().startsWith("vm")) {

                    String[] inputarray = input.trim().split(" ");
                    if (inputarray.length == 3) {

                        if (inputarray[0].equals("host")) {

                            if (inputarray[2].equals("info")) {
                                getHostInfo(currentService, inputarray[1]);
                            }

                            if (inputarray[2].equalsIgnoreCase("datastore")) {
                                getHostDatastore(currentService, inputarray[1]);
                            }

                            if (inputarray[2].equalsIgnoreCase("network")) {
                                getHostNetworkInfo(currentService, inputarray[1]);
                            }
                        }

                        if (inputarray[0].equals("vm")) {

                            if (inputarray[2].equals("info")) {
                                getVMInfo(currentService, inputarray[1]);
                            }

                            if (inputarray[2].equals("on")) {
                                toggleVMState(currentService, inputarray[1], true);
                            }

                            if (inputarray[2].equals("off")) {
                                toggleVMState(currentService, inputarray[1], false);
                            }

                            if (inputarray[2].equals("shutdown")) {
                                shutDownVM(currentService, inputarray[1]);
                            }

                        }

                    } else {
                        System.out.println("Invalid command");
                    }

                } else {
                    System.out.println("Invalid command");
                }
            }

        } else {
            System.out.println("Unable to connect to vcenter");
        }
    }


    private static ServiceInstance loginToVcenter(String[] args) {
        if (args == null) {
            System.out.println("Please Enter ip address username password");
            return null;
        } else {
            try {
                return new ServiceInstance(new URL(args[0]), args[1], args[2], true);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private static void printHelpPage() {
        System.out.println("usage");
        System.out.println("exit                   exit the program");
        System.out.println("host                   enumerate hosts");
        System.out.println("host hname info        show info for hname");
        System.out.println("host hname datastore   enumerate datastores for hname");
        System.out.println("host hname network     enumerate networks for hname");
        System.out.println("vm                     enumerate vms");
        System.out.println("vm vname info           show info for vname");
        System.out.println("vm vname shutdown       shutdown os on vm");
        System.out.println("vm vname on             power on vname");
        System.out.println("vm vname off            power off vname");
    }


    static void getAllVms(ServiceInstance instance) {
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

    static void getVMInfo(ServiceInstance instance, String vmName) {
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
                while (machine.getRuntime().getPowerState() == VirtualMachinePowerState.poweredOn);

                if (machine.getRuntime().getPowerState() == VirtualMachinePowerState.poweredOff) {
                    System.out.println("Name = " + vmName);
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                    System.out.println("Shutdown guest :completed " + formatter.format(new Date(System.currentTimeMillis())));
                    stopTime.cancel();
                }

            } else {
                System.out.println("invalid vm name");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Graceful shutdown failed now trying hard power off");
            toggleVMState(instance, vmName, false);
        }
    }


    public static void displayAllHosts(ServiceInstance instance) {
        Folder rootFolder = instance.getRootFolder();
        try {
            ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
            int i = 0;
            for (ManagedEntity entity : managedEntities) {
                HostSystem host = (HostSystem) entity;
                if (host != null) {
                    String ipAddress = host.getName();
                    System.out.println("host[" + i + "] : Name = " + ipAddress);
                    i++;
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void getHostInfo(ServiceInstance instance, String address) {
        Folder rootFolder = instance.getRootFolder();
        try {
            ManagedEntity entity = new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", address);
            if (entity != null) {
                HostSystem host = (HostSystem) entity;
                System.out.println("Name = " + host.getName());
                System.out.println("ProductNameFull = " + host.getConfig().product.getFullName());
                System.out.println("CPU cores = " + host.getHardware().cpuInfo.getNumCpuCores());
                System.out.println("RAM = " + host.getHardware().getMemorySize() / (1000 * 1000 * 1000) + " GB");
            } else {
                System.out.println("invalid host name = " + address);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void getHostDatastore(ServiceInstance instance, String address) {
        Folder rootFolder = instance.getRootFolder();
        try {
            ManagedEntity entity = new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", address);
            if (entity != null) {
                HostSystem host = (HostSystem) entity;
                Datastore[] datastores = host.getDatastores();
                int i = 0;
                for (Datastore store : datastores) {
                    System.out.println("Datastore[" + i + "] : name = " + store.getName()
                            + " capacity = " + store.getSummary().getCapacity() / (1000 * 1000 * 1000) + " GB"
                            + " FreeSpace = " + store.getSummary().getFreeSpace() / (1000 * 1000 * 1000) + " GB");
                    i++;
                }

            } else {
                System.out.println("invalid host name = " + address);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public static void getHostNetworkInfo(ServiceInstance instance, String address) {
        Folder rootFolder = instance.getRootFolder();
        ManagedEntity entity = null;
        try {
            entity = new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", address);
            if (entity != null) {
                HostSystem host = (HostSystem) entity;
                Network[] networks = host.getNetworks();
                int i = 0;
                for (Network network : networks) {
                    System.out.println("Network[" + i + "] : name = " + network.getName());
                    i++;
                }

            } else {
                System.out.println("invalid host name = " + address);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
