package src;

import java.util.*;
import java.lang.Object;
import java.io.*;
import java.io.DataInputStream;
import Dropboxlike.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.security.MessageDigest;

public class DropboxLikeImpl extends RepositoryPOA {
    private ArrayList<FileAtRepository> repo = new ArrayList<FileAtRepository>();
    List repository = Collections.synchronizedList(repo);
    private UserManagerImpl um;
    private String server_home;
    private ORB orb;
    DropboxLikeImpl() {
        repository =  new ArrayList<FileAtRepository>();
        um = new UserManagerImpl();
        server_home= new String("");
    }
    public String SHAchecksumpassword(String password){
        StringBuffer sb = new StringBuffer();
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] dataBytes = new byte[password.length()];
            dataBytes = password.getBytes();
            md.update(dataBytes, 0, password.length());
            byte[] mdbytes = md.digest();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public void set_home(String server_home) {
        this.server_home = server_home;
    }

    public String get_home() {
        return server_home;
    }

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public boolean isLogged(String username, String token) {
        return um.isLogged(username, token);
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


    public boolean send(FileAtRepository file, String username, String token ) {
        if(um.isLogged(username,token))
        {
            synchronized(repository) {
                repository.add(file);
            }
            try{
                File user_dir = new File (server_home+"/"+username);
                user_dir.mkdirs();
                FileOutputStream writer = new FileOutputStream(server_home+"/"+username+"/"+file.name);
                writer.write(file.cont);
                writer.close();
                File w = new File (server_home+"/"+username+"/.user.info");
                if(!w.exists())
                    w.createNewFile();
                FileWriter  fileWri = new FileWriter(server_home+"/"+username+"/.user.info", true);
                BufferedWriter  bufWri = new BufferedWriter(fileWri);
                PrintWriter out = new PrintWriter(bufWri);
                out.println(file.name+":"+file.md5+":"+file.ownerUserName);
                out.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
    public boolean updateFile(FileAtRepository file, String username, String token) {
        if(um.isLogged(username, token)) {
            synchronized(repository) {
                FileAtRepository toRemove = new FileAtRepository();
                for (Object e : repository) {
                    if (((FileAtRepository)e).name.compareTo(file.name) == 0) {
                        toRemove = (FileAtRepository)e;
                    }
                }
                try {
                    FileOutputStream writer = new FileOutputStream(server_home+"/"+username+"/"+file.name);
                    writer.write(file.cont);
                    writer.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }

                repository.remove(toRemove);
                repository.add(file);
            }
            SmallL[] list = askListUser(username, token);
            File temp_ = new File (server_home+"/"+username+"/.user.info");
            temp_.delete();
            temp_ = new File(server_home+"/"+username+"/.user.info");
            PrintWriter tempw = null;
            try{
                tempw = new PrintWriter(temp_);
            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            }
            for (SmallL el : list) {
                if (el.name.compareTo(file.name)==0) {
                    tempw.println(file.name+":"+file.md5+":"+username);
                }
                else {
                    tempw.println(el.name+":"+el.md5+":"+username);
                }
            }
            tempw.close();
        }
        return false;
    }
    public SmallL[] askListUser (String username, String token) {
        ArrayList<SmallL> list = new ArrayList<SmallL>();
        File userlist = new File(server_home+"/"+username+"/.user.info");
        try{
            if(!userlist.exists())
                userlist.createNewFile();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Scanner sc=null;
        try {
            sc = new Scanner(userlist);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(sc.hasNext()) {
            String line = sc.nextLine();
            String[] tmp = line.split(":");
            list.add(new SmallL(tmp[0],tmp[1]));
        }
        SmallL[] smallL;
        if (list.size() != 0)
            smallL = list.toArray(new SmallL[list.size()]);
        else {
            smallL = new SmallL[1];
            smallL[0] = new SmallL("NULL","NULL");
        }
        return smallL;
    }

    public boolean subscribe(String name,String surname, String username, String password) {
            if(um.subscribe(name, surname, username, password)) {
                File user_dir = new File(server_home+"/"+username);
                user_dir.mkdir();
                File w = new File (server_home+"/users_list.txt");
                try{
                    if(!w.exists())
                        w.createNewFile();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                FileWriter fileWri = null;
                try{
                    fileWri = new FileWriter(server_home+"/users_list.txt", true);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedWriter  bufWri = new BufferedWriter(fileWri);
                PrintWriter out = new PrintWriter(bufWri);
                out.println(name+":"+surname+":"+username+":"+SHAchecksumpassword(password));
                out.close();
                return true;
            }
            else {
                return false;
            }
    }

    public String login (String username, String password, String dev_id) {
        return um.login(username, password,dev_id);
    }

    public boolean logout(String username, String dev_id, String token) {
        return um.logout(username, dev_id, token);
    }

    public boolean remove(String username, String password) {
        if(um.remove(username, password)) {
            ArrayList<UserInfo> users = new ArrayList<UserInfo>();
            File user_dir = new File(server_home+"/"+username);
            try {
                delete(user_dir);
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            File users_file = new File(server_home+"/users_list.txt");
            Scanner sc = null;
            try {
                sc = new Scanner(users_file);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (sc.hasNext()) {
                String line = sc.nextLine();
                System.out.println(line);
                String[] lines=  line.split(":");
                users.add(new UserInfo(lines[0],lines[1],lines[2], lines[3]));
            }
            users_file.delete();
            FileWriter users_files = null;
            try {
                users_files = new FileWriter(server_home+"/users_list.txt",true);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter tempw =  new PrintWriter(users_files);
            for(UserInfo el : users) {
                tempw.println(el.name+":"+el.surname+":"+el.username+":"+el.password);
            }
            tempw.close();

            return false;
        }
        else {
            return false;
        }
    }
    public FileAtRepository get_file(String username, String token, String filename) {
        FileAtRepository toReturn = new FileAtRepository();
        toReturn.name = "NULL";
        toReturn.md5 = "NULL";
        toReturn.ownerUserName = "NULL";

        if (um.isLogged(username, token)) {
            synchronized(repository) {
                for (Object e : repository) {
                    if (((FileAtRepository)e).name.compareTo(filename) == 0) {
                        try {
                            File toRead = new File (server_home+"/"+username+"/"+filename);
                            byte[] fileData = new byte[(int) toRead.length()];
                            DataInputStream  dis = new DataInputStream(new FileInputStream(toRead));
                            dis.readFully(fileData);
                            dis.close();
                            toReturn.name = ((FileAtRepository)e).name;
                            toReturn.md5 = ((FileAtRepository)e).md5;
                            toReturn.ownerUserName =((FileAtRepository)e).ownerUserName;
                            toReturn.cont = fileData;
                        }
                        catch (IOException el) {
                            el.printStackTrace();
                        }
                        return toReturn;
                    }
                }
            }
            return toReturn;
        }
        else {
            return toReturn;
        }
    }
    public boolean delete (String filename, String username, String token) {
        synchronized(repository) {
            for (Object f : repository) {
                if (((FileAtRepository)f).ownerUserName.compareTo(username) == 0 && ((FileAtRepository)f).name.compareTo(filename)== 0) {
                    try{
                        File file = new File(server_home+"/"+username+"/"+((FileAtRepository)f).name);
                        file.delete();
                        SmallL[] list = askListUser(username, token);
                        File temp_ = new File (server_home+"/"+username+"/.user.info");
                        temp_.delete();
                        temp_ = new File(server_home+"/"+username+"/.user.info");
                        PrintWriter tempw = new PrintWriter(temp_);
                        for (SmallL el : list) {
                            if (el.name.compareTo(filename) != 0) {
                                tempw.println(el.name+":"+el.md5+":"+username);
                            }
                        }
                        tempw.close();
                        return true;
                    }
                    catch (Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public void bootstrap (){
        ArrayList<UserInfo> users = new ArrayList<UserInfo>();
        File users_file = new File(server_home+"/users_list.txt");
        try {
            if (!users_file.exists())
                users_file.createNewFile();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Scanner sc = null;
        try {
            sc = new Scanner(users_file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (sc.hasNext()) {
            String line = sc.nextLine();
            System.out.println(line);
            String[] lines=  line.split(":");
            users.add(new UserInfo(lines[0],lines[1],lines[2], lines[3]));
        }
        for(UserInfo e : users) {
            um.record(e.name, e.surname, e.username, e.password);
        }
    }
    public boolean check_username(String username) {
        return um.check_username(username);
    }
    public void shutdown() {
            orb.shutdown(false);
    }
}

