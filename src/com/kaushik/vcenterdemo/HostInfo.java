package com.kaushik.vcenterdemo;

import com.vmware.vim25.mo.*;

import java.rmi.RemoteException;

/**
 * Created by kaushik on 11/10/16.
 */
public class HostInfo {


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
                            + " capacity = " + store.getInfo().maxVirtualDiskCapacity/ (1000 * 1000 * 1000) + " GB"
                            + "FreeSpace = " + store.getInfo().freeSpace/ (1000 * 1000 * 1000) + " GB");
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
