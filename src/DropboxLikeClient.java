package src;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import Dropboxlike.*;
import java.util.*;
import java.io.*;
import java.io.DataInputStream;
import java.security.MessageDigest;

public class DropboxLikeClient {
    static Repository dropboxImpl;
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
        System.out.println ("/*************************  DropboxLike   ***************************/");
        System.out.println ("/***********  Author : Andrea Mambretti   Version 1.0   *************/ ");
        System.out.println ("/********************************************************************/ ");
        System.out.println ("Select an operation:");
        System.out.println ("");
        System.out.println ("    1) Subscribe an account ");
        System.out.println ("    2) Remove account");
        System.out.println ("    3) Login");
        System.out.println ("    4) Logout");
        System.out.println ("    5) Send file");
        System.out.println ("    6) Remove file");
        System.out.println ("    7) Exit");
        System.out.println ("");
        System.out.print ("dropboxlike > ");
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

    }

    public static void logout() {

    }

    public static void send_file() {

    }

    public static void remove_file() {

    }

    public static void main(String[] args) {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            //  get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            String name = "DBServer";
            dropboxImpl = RepositoryHelper.narrow(ncRef.resolve_str(name));
            Console con = System.console();
            Scanner read = new Scanner(con.reader());
            int c=0;
            while (c != 7) {
                do {
                    menu();
                    c = read.nextInt();
                } while(c < 0 || c > 8);
                if (c == 1) {
                    subscribe();
                }
                else if (c == 2) {
                    remove_account();
                }
                else if (c == 3) {
                    login();
                }
                else if (c == 4) {
                    logout();
                }
                else if (c == 5) {
                    send_file();
                }
                else if (c == 6) {
                    remove_file();
                }
            }




    /*        char c='r';
            File fis = new File("/home/m4mbr3/documents/bording_pass/rodi/BoardingPass.pdf");
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
            while (c != 'e') {
                System.out.println(dropboxImpl.send(entity, "mambro", token ));
                c = 'e';
            }
            //dropboxImpl.remove("mambro","my_passwd");
            dropboxImpl.logout("mambro","EE:EE:EE:EE:EE:EE");*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
