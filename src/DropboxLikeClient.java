package src;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import Dropboxlike.*;
import java.util.*;
import java.io.*;
import java.io.DataInputStream;
import java.security.MessageDigest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.net.NetworkInterface;

public class DropboxLikeClient {
    static Repository dropboxImpl;
    static String token;
    static String user_name;
    static String user_dev_id;


    static String SHAchecksumfile(String path) {
        StringBuffer hexString = new StringBuffer();
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            FileInputStream fis = new FileInputStream(path);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 2
                        for (int i=0;i<mdbytes.length;i++) {
                hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }



    public static void menu() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println ("/*************************  DropboxLike   ***************************/");
        System.out.println ("/***********  Author : Andrea Mambretti   Version 1.0   *************/ ");
        System.out.println ("/********************************************************************/ ");
        System.out.println ("Select an operation:");
        System.out.println ("");
        System.out.println ("    subscribe");
        System.out.println ("    remove_account");
        System.out.println ("    login");
        System.out.println ("    logout");
        System.out.println ("    send_file");
        System.out.println ("    remove_file");
        System.out.println ("    clear");
        System.out.println ("    help");
        System.out.println ("    exit");
        System.out.println ("");
    }

    public static void subscribe() {
        Console c  = System.console();
        String name;
        String surname;
        String username;
        String password = new String();
        name = c.readLine("Insert your name: ");
        surname = c.readLine("Insert your surname: ");
        boolean first = true;
        do {
            if (first) {
                username = c.readLine("Insert your username: ");
                first = false;
            }
            else
                username = c.readLine("Username not available. Try again: ");
        }while (!dropboxImpl.check_username(username));
        boolean noMatch;
        do {
            char [] newPassword1 = c.readPassword("Enter your new password: ");
            char [] newPassword2 = c.readPassword("Enter new password again: ");
            noMatch = ! Arrays.equals(newPassword1, newPassword2);
            if (noMatch) {
                c.format("Passwords don't match. Try again.%n ");
            } else {
                password = new String(newPassword1);
            }
            Arrays.fill(newPassword1, ' ');
            Arrays.fill(newPassword2, ' ');
        } while (noMatch);
        dropboxImpl.subscribe(name,surname, username, password);
    }

    public static void remove_account() {
        Console c = System.console();
        String username;
        String password;
        boolean first = true;
        do {
            if (!first)
                 System.out.println("Error: maybe the user or/and the password are wrong ");
            username = c.readLine("Insert your username: ");
            char[] password_ = c.readPassword("Insert the password for " + username + ": ");
            password = new String(password_);
            first = true;

        }while(!dropboxImpl.remove(username, password));
    }

    public static void login() {
        if (!dropboxImpl.isLogged(user_name, token)) {
            String username;
            String password;
            String dev_id = new String();
            Console c = System.console();
            boolean fail = false;
            do {
                try{
                    if (!fail) {
                        InetAddress ip = InetAddress.getLocalHost();
                        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                        byte[] mac = network.getHardwareAddress();
                        if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i < mac.length; i++) {
                            sb.append(String.format("%20X%s", mac[i], (i < mac.length -1) ? "-" : ""));
                        }
                        dev_id = sb.toString();
                        }
                        else fail = true;
                    }
                    else {
                         System.out.println("Error while I was reading the mac address so I will try to use the hostname of your machine");
                        dev_id = InetAddress.getLocalHost().getHostName();
                        fail = false;
                    }
                }
                catch (UnknownHostException e) {
                    fail = true;
                }
                catch (SocketException e) {
                    fail = true;
                }
            }while (fail);
            username = c.readLine("Insert your username: ");
            char[] pas = c.readPassword("Insert the password for " + username + ": ");
            password = new String(pas);
            String res = dropboxImpl.login(username,password,dev_id);
            if (res.compareTo("INVALID_USER") == 0) {
                System.out.println("Error: maybe the user or/and the password are wrong");
                return;
            }
            else {
                user_name = username;
                user_dev_id = dev_id;
                token = res;
            }
        }
        else {
            System.out.println("You are already logged!!!");
            System.out.println("If you want to log in with a different user, please log out first");
        }
    }

    public static void logout() {
        if (!dropboxImpl.isLogged(user_name, token) ) {
            System.out.println("Error: you are not logged");
            return;
        }
        else {
            dropboxImpl.logout(user_name,user_dev_id,token);
            user_name = "";
            user_dev_id = "";
            token = "";
        }
    }

    public static void send_file() {
        if (!dropboxImpl.isLogged(user_name, token)) {
            System.out.println("Error: you are not logged");
            return;
        }
        else {
            //TODO Insert FileAtRepository data
            //dropboxImpl.send();
        }
    }

    public static void remove_file() {

    }

    public static void main(String[] args) {
        token = "";
        user_name = "";
        user_dev_id = "";
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            //  get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            String name = "DBServer";
            dropboxImpl = RepositoryHelper.narrow(ncRef.resolve_str(name));
            Console con = System.console();
            String c="";
            menu ();
            while (c.compareTo("exit") != 0) {
                if(user_name.compareTo("") == 0)
                    c = con.readLine("dropboxlike $ ").trim();
                else
                    c = con.readLine(user_name+"@dropboxlike $ ").trim();
                if (c.compareTo("subscribe") == 0) {
                    subscribe();
                }
                else if (c.compareTo("remove_account") == 0) {
                    remove_account();
                }
                else if (c.compareTo("login") == 0) {
                    login();
                }
                else if (c.compareTo("logout") == 0) {
                    logout();
                }
                else if (c.compareTo("send_file") == 0) {
                    if (dropboxImpl.isLogged(user_name, token)) {
                        send_file();
                    }
                    else {
                        System.out.println("Error: you are not logged");
                    }
                }
                else if (c.compareTo("remove_file") == 0) {
                    remove_file();
                }
                else if (c.compareTo("help") == 0) {
                    menu();
                }
                else if (c.compareTo("clear") == 0) {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
                else if (c.compareTo("exit") != 0) {
                    System.out.println("dropboxlike:"+ c + ":command not found");
                }
            }
            if (dropboxImpl.isLogged(user_name, token)) {
                    logout();
            }
           /* File fis = new File("/home/m4mbr3/documents/bording_pass/rodi/BoardingPass.pdf");
            byte[] fileData = new byte[(int) fis.length()];
            DataInputStream dis = new DataInputStream((new FileInputStream(fis)));
            dis.readFully(fileData);
            dis.close();
            FileAtRepository entity = new FileAtRepository();
            entity.ownerUserName = "mambro";
            entity.md5 = SHAchecksumfile("/home/m4mbr3/documents/bording_pass/rodi/BoardingPass.pdf");
            entity.cont = fileData;
            entity.name = "BoardingPass.pdf";
            dropboxImpl.subscribe("Andrea","Mambretti","mambro", "my_passwd");
            String token = dropboxImpl.login("mambro", "my_passwd","EE:EE:EE:EE:EE:EE");
            //while (c != 'e') {
                System.out.println(dropboxImpl.send(entity, "mambro", token ));
             //   c = 'e';
            //}
            //dropboxImpl.remove("mambro","my_passwd");
            //dropboxImpl.logout("mambro","EE:EE:EE:EE:EE:EE");
            */
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
