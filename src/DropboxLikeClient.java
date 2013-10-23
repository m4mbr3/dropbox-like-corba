package src;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import Dropboxlike.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
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
    static String home_env;
    static ArrayList<SmallL> user_file =  new ArrayList<SmallL>();
    static List user_file_list = Collections.synchronizedList(user_file);
    static boolean runn = true;
    static Thread updater;
    public DropboxLikeClient(){
        updater = null;
    }
    public static void runSyncThread() {
        runn = true;
        Thread t = new Thread(new Runnable() {
            public void run() {
                String username = user_name;
                String token_ = token;
                syncThread(username,token_);
            }
        });
        t.start();
    }

    public static void runUpdateThread() {
        runn = true;
        Thread t = new Thread(new Runnable() {
            public void run() {
                String username = user_name;
                String token_ = token;
                updateThread(username, token_);
            }
        });
        t.start();
    }

    public static void updateThread(String username, String token_) {
        while (runn) {
            synchronized (user_file_list) {
                for (int i = 0; i < user_file_list.size(); i++) {
                    String md5 = SHAchecksumfile(home_env+"/"+username+"/"+((SmallL)user_file_list.get(i)).name).trim();
                    if(((SmallL)user_file_list.get(i)).md5.compareTo(md5) != 0) {
                        //saved memory local copy
                        ((SmallL)user_file_list.get(i)).md5 = md5;
                        File element = new File (home_env + "/" + username + "/" + ((SmallL)user_file_list.get(i)).name);
                        try {
                            //send to the repository the update
                            byte[] fileData = new byte[(int) element.length()];
                            DataInputStream dis = new DataInputStream((new FileInputStream(element)));
                            dis.readFully(fileData);
                            dis.close();
                            FileAtRepository entity = new FileAtRepository();
                            entity.ownerUserName = username;
                            entity.md5 = md5;
                            entity.cont = fileData;
                            entity.name = element.getName();
                            dropboxImpl.updateFile(entity, username, token);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //update the local metadata
                File temp_ = new File(home_env+"/"+username+"/.data");
                temp_.delete();
                temp_ = new File(home_env+"/"+username+"/.data");
                PrintWriter tempw = null;
                try {
                    tempw = new PrintWriter(temp_);
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                for ( Object el : user_file_list) {
                    tempw.println(((SmallL)el).name+":"+((SmallL)el).md5+":"+username);
                }
                tempw.close();
            }
            try{
                Thread.sleep(10000);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    public static void syncThread (String username, String token) {
        while(runn) {
            synchronized (user_file_list) {
                SmallL[] listFromServer = null;
                try {
                    listFromServer = dropboxImpl.askListUser(username, token);
                    if(listFromServer.length != 0) {
                        SmallL[] listFromClient = (SmallL[]) user_file_list.toArray(new SmallL[user_file_list.size()]);
                        for (SmallL el1 : listFromServer) {
                            boolean find = false;
                            boolean modified = false;
                            SmallL toRemove = null;
                            for (SmallL el2 : listFromClient) {
                                if(el1.name.compareTo(el2.name) == 0 && el1.md5.compareTo(el2.md5) == 0) {
                                    find = true;
                                }
                                else if (el1.name.compareTo(el2.name) == 0 && el1.md5.compareTo(el2.md5) != 0) {
                                    modified = true;
                                    toRemove = el2;
                                }
                            }
                            if(!find && !modified) {
                                FileAtRepository new_file = null;
                                FileOutputStream writer = null;
                                try{
                                    if(el1.name.compareTo("NULL") != 0) {
                                        new_file = dropboxImpl.get_file(username, token, el1.name );
                                        if (new_file.name.compareTo("NULL") != 0) {
                                            writer = new FileOutputStream(home_env+"/"+username+"/"+new_file.name);
                                            writer.write(new_file.cont);
                                            writer.close();
                                            SmallL toAdd = new SmallL(new_file.name, new_file.md5);
                                            user_file_list.add(toAdd);
                                            File data = new File(home_env+"/"+username+"/.data");
                                            if(!data.exists()){
                                                data.createNewFile();
                                            }
                                            FileWriter fileWri =  new FileWriter(home_env+"/"+username+"/.data");
                                            BufferedWriter bufWri =  new BufferedWriter(fileWri);
                                            PrintWriter out = new PrintWriter(bufWri);
                                            out.println(new_file.name+":"+new_file.md5+":"+new_file.ownerUserName);
                                        }
                                    }
                                }
                                catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(modified) {
                                FileAtRepository new_file = null;
                                FileOutputStream writer = null;
                                try {
                                    if (el1.name.compareTo("NULL") != 0) {
                                        new_file = dropboxImpl.get_file(username, token, el1.name);
                                        if (new_file.name.compareTo("NULL") != 0) {
                                            writer = new FileOutputStream(home_env+"/"+username+"/"+new_file.name);
                                            writer.write(new_file.cont);
                                            writer.close();
                                            SmallL toAdd =  new SmallL(new_file.name, new_file.md5);
                                            user_file_list.remove(toRemove);
                                            user_file_list.add(toAdd);
                                            File data = new File(home_env+"/"+username+"/.data");
                                            data.delete();
                                            data = new File(home_env+"/"+username+"/.data");
                                            PrintWriter out =  new PrintWriter(data);
                                            out.println(new_file.name+":"+new_file.md5+":"+new_file.ownerUserName);
                                        }
                                    }
                                }
                                catch(FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                catch(IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        for (SmallL el1 : listFromClient) {
                            boolean find = false;
                            for (SmallL el2 : listFromServer) {
                                if(el1.name.compareTo(el2.name) == 0) {
                                    find = true;
                                }
                            }
                            if(!find) {
                                File toDelete =  new File (home_env+"/"+username+"/"+el1.name);
                                toDelete.delete();
                                user_file_list.remove((Object)el1);
                            }
                        }
                    }
                }
                catch (OwnerException e) {
                    e.printStackTrace();
                }
                catch (TokenException e) {
                    e.printStackTrace();
                }
                catch (FileDoesntExist e) {
                    e.printStackTrace();
                }
            }
            try{
                Thread.sleep(1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void stopAllThread() {
        runn =  false;
    }
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
        System.out.println ("/***********  Author : Andrea Mambretti   Version 1.1   *************/ ");
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
        System.out.println ("    dir");
        System.out.println ("    ls");
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
        File dir = new File(home_env+"/"+user_name);
        dir.mkdirs();
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
        File  path =  new File(home_env + "/" + user_name);
        try {
            delete(path);
        }
        catch(IOException e) {
            System.out.println("Error: Something went wrong while deleting the directory");
        }
        user_name = "";
        token = "";
        user_dev_id = "";
    }

    public static void load_list() {
        File file = new File(home_env + "/"+ user_name + "/.data");
        try{
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line =  sc.nextLine();
                String[] tmp = line.split(":");
                synchronized (user_file_list) {
                    user_file_list.add((Object) new SmallL(tmp[0],tmp[1]));
                }
            }
        }
        catch (FileNotFoundException e) {
            File path = new File (home_env + "/" + user_name);
            File data = new File (home_env + "/" + user_name + "/.data");
            try {
                path.mkdirs();
                data.createNewFile();
            }
            catch(IOException er) {
                System.out.println ("Error: Impossible to create the metadata in your repository");
                System.out.println ("Please check to be able to write at" + home_env);
                System.out.println ("If no consider to change the base dir");
            }
        }
    }
    public static void delete (File file) throws IOException {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                file.delete();
            }
            else {
                String files[] = file.list();
                for (String temp :files) {
                    File fileToDelete = new File(file, temp);
                    delete(fileToDelete);
                }
                if(file.list().length == 0) {
                    file.delete();
                }
            }
        }
        else {
            file.delete();
        }
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
            dev_id = c.readLine("Insert your hostname: ");
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
                load_list();
                runSyncThread();
                runUpdateThread();
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
            synchronized (user_file_list) {
                user_file =  new ArrayList<SmallL>();
                user_file_list = Collections.synchronizedList(user_file);
            }
            stopAllThread();
        }
    }

    public static void send_file() {
        if (!dropboxImpl.isLogged(user_name, token)) {
            System.out.println("Error: you are not logged");
            return;
        }
        else {
            Console c = System.console();
            File element = null;
            boolean exist = true;
            String path = null;
            while(exist) {
                try{
                    path = c.readLine("Insert the _complete_ path to the file to upload: ");
                    element = new File(path);
                    boolean isReadable = element.canRead();
                    boolean isDirectory = element.isDirectory();
                    if (isDirectory) throw new FileNotFoundException();
                    if (isReadable)
                        exist = false;
                    else
                        throw new IOException();
                }
                catch (FileNotFoundException e) {
                    System.out.println("Error: File either does not exist or is a directory. Try again...");
                }
                catch (IOException e){
                    System.out.println("Error: File either does not readable or does not exist. Try again...");
                }

            }
            try{
                byte[] fileData = new byte[(int) element.length()];
                DataInputStream dis = new DataInputStream((new FileInputStream(element)));
                dis.readFully(fileData);
                dis.close();
                FileAtRepository entity = new FileAtRepository();
                entity.ownerUserName = user_name;
                entity.md5 = SHAchecksumfile(path);
                entity.cont = fileData;
                entity.name = element.getName();
                synchronized (user_file_list) {
                    for (Object l : user_file_list) {
                        if (((SmallL)l).name.compareTo(entity.name) == 0 && ((SmallL)l).md5.compareTo(entity.md5) == 0){
                            System.out.println("Error: The file is already in your repository");
                            return;
                        }
                    }
                    //Create a copy in the local repository
                    FileOutputStream local_copy = new FileOutputStream (home_env+"/"+user_name+"/"+entity.name);
                    local_copy.write(entity.cont);
                    local_copy.close();
                    //Updating of local metadata
                    File data = new File(home_env+"/"+user_name+"/.data");
                    if(!data.exists()) {
                        data.createNewFile();
                    }
                    FileWriter fileWri = new FileWriter(home_env+"/"+user_name+"/.data", true);
                    BufferedWriter bufWri = new BufferedWriter (fileWri);
                    PrintWriter out = new PrintWriter(bufWri);
                    out.println(entity.name+":"+entity.md5+":"+entity.ownerUserName);
                    out.close();
                    //Updating the user_list
                    user_file_list.add(new SmallL(entity.name, entity.md5));
                    //Send file to remote server
                    dropboxImpl.send(entity, user_name, token);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (TokenException e) {
                e.printStackTrace();
            }
            catch (ErrorFileSending e) {
                e.printStackTrace();
            }
        }
    }

    public static void remove_file() {
        if (!dropboxImpl.isLogged(user_name,token)) {
            System.out.println("Error: you are not logged");
            return;
        }
        else {
            Console con = System.console();
            String filename;
            boolean correct = true;
            SmallL toDelete = null;
            synchronized (user_file_list) {
                do{
                    dir();
                    filename = con.readLine("Insert the file to delete: ").trim();
                    for (Object element : user_file_list) {
                        if (((SmallL)element).name.compareTo(filename) == 0) {
                            correct = false;
                            toDelete = ((SmallL)element);
                        }
                    }
                    if (correct) {
                        System.out.println("Error: the filename provided is not valid. Try again...");
                    }
                }while(correct);
                try {
                    if (dropboxImpl.delete(filename,user_name, token)) {
                        //delete element from user_file_list
                        user_file_list.remove(toDelete);
                        File file_to_delete = new File(home_env+ "/" + user_name+"/"+toDelete.name);
                        file_to_delete.delete();
                        File temp_ = new File(home_env+"/"+user_name+"/.data");
                        temp_.delete();
                        temp_ = new File(home_env+"/"+user_name+"/.data");
                        PrintWriter tempw = new PrintWriter(temp_);
                        for ( Object el : user_file_list) {
                            tempw.println(((SmallL)el).name+":"+((SmallL)el).md5+":"+user_name);
                        }
                        tempw.close();
                    }
                    else {
                        System.out.println("It is not possible to remove the file now. Please try later");
                    }
                }
                catch (OwnerException e) {
                    e.printStackTrace();
                }
                catch (FileDoesntExist e) {
                    e.printStackTrace();
                }
                catch (TokenException e) {
                    e.printStackTrace();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void dir() {
        if (!dropboxImpl.isLogged(user_name,token)) {
            System.out.println("Error: you are not logged");
            return;
        }
        else {
            int i=0;
            System.out.println("Your repository contains: ");
            synchronized (user_file_list) {
                if(user_file_list != null)
                    for (Object l : user_file_list) {

                        System.out.println(i+") "+ ((SmallL)l).name);
                        i++;
                    }
                if(i == 0)
                    System.out.println("No file found in your repository");
            }
        }
    }

    public static void main(String[] args) {
        token = "";
        user_name = "";
        user_dev_id = "";
        home_env = System.getenv("DROPBOXLIKECLIENT_HOME");
        if (home_env == null) home_env="dropboxlikeclient";
        else
            home_env = home_env + "/dropboxlikeclient";
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
                    send_file();
                }
                else if (c.compareTo("remove_file") == 0) {
                    remove_file();
                }
                else if (c.compareTo("help") == 0) {
                    menu();
                }
                else if (c.compareTo("dir") == 0 || c.compareTo("ls")==0) {
                    dir();
                }
                else if (c.compareTo("clear") == 0) {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
                else if (c.compareTo("exit") != 0) {
                    System.out.println("dropboxlike: "+ c + ": command not found");
                }
            }
            if (dropboxImpl.isLogged(user_name, token)) {
                    logout();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
