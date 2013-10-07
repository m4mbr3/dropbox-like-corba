package src;

import java.util.*;
import java.io.*;
import Dropboxlike.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

public class DropboxLikeImpl extends RepositoryPOA {
    private ArrayList<FileAtRepository> repository;

    private UserManagerImpl um;
    private String server_home;
    private ORB orb;
    DropboxLikeImpl() {
        repository =  new ArrayList<FileAtRepository>();
        um = new UserManagerImpl();
        server_home= new String("");
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
            repository.add(file);
            try{
                File user_dir = new File (server_home+"/"+username);
                user_dir.mkdirs();
                FileOutputStream writer = new FileOutputStream(server_home+"/"+username+"/"+file.name);
                writer.write(file.cont);
                writer.close();
                PrintWriter w = new PrintWriter (server_home+"/"+username+"/"+file.name+".info");
                w.println(file.name+": "+file.md5+":"+file.ownerUserName);
                w.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public SmallL[] askListUser (String username, String token) {
        ArrayList<SmallL> list = new ArrayList<SmallL>();
        for (FileAtRepository f : repository) {
            if (f.ownerUserName.equals(username)) {
                list.add(new SmallL(f.name, f.md5));
            }
        }
         SmallL[] smallL = list.toArray(new SmallL[list.size()]);
        return smallL;
    }

    public boolean subscribe(String name,String surname, String username, String password) {
            if(um.subscribe(name, surname, username, password)) {
                File user_dir = new File(server_home+"/"+username);
                user_dir.mkdir();
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
            File user_dir = new File(server_home+"/"+username);
            try {
                delete(user_dir);
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        else {
            return false;
        }
    }

    public boolean delete (String filename, String username, String token) {
        for (FileAtRepository f : repository) {
            if (f.ownerUserName.equals(username) && f.name.equals(filename)) {
                try{
                    File file = new File(server_home+"/"+username+"/"+f.name);
                    if (file.delete())
                        System.out.println(f.name + "deleted");
                    else
                        System.out.println("File Delation Fail");
                    file = new File(server_home+"/"+username+"/"+f.name+".info");
                    if (file.delete())
                        System.out.println("Metadata file deleted");
                    else
                        System.out.println("File Delation Fail");
                    return true;
                }
                catch (Exception e ) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean check_username(String username) {
        return um.check_username(username);
    }
    public void shutdown() {
            orb.shutdown(false);
            }
}

