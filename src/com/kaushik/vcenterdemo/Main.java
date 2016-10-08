package com.kaushik.vcenterdemo;

import com.vmware.vim25.mo.ServiceInstance;

import java.net.MalformedURLException;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Scanner;

//WSClient invoke

public class Main {

    public static void main(String[] args) {
        System.out.println("CMPE 281 HW2 from Kaushik Shingne");

        //Login to server and retrive service instance
        ServiceInstance currentService = loginToVcenter(args);
        if (currentService != null) {
            Scanner enter = new Scanner(System.in);

            while (true) {
                System.out.print("kaushik-8924 >");
                switch (enter.next()) {

                    case "exit":
                        System.exit(0);
                        break;

                    case "help":
                        printHelpPage();

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


}
