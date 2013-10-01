package src;

import java.util.*;
import java.io.*;
import Dropboxlike.*;
import org.omg.CORBA.*;

public class DropboxLikeImpl extends RepositoryPOA {
    private ArrayList<FileAtRepository> repository;

    private UserManagerImpl um;

    DropboxLikeImpl() {
        repository =  new ArrayList<FileAtRepository>();
        um = new UserManagerImpl();
    }
    public boolean send(FileAtRepository file, String username, String token ) {
        if(um.isLogged(username,token))
        {
            repository.add(file);
            try{
                FileOutputStream writer = new FileOutputStream(username+"/"+file.name);
                writer.write(file.cont);
                writer.close();
                PrintWriter w = new PrintWriter (username+"/"+file.name+".info");
                w.println(file.name+":"+file.md5+":"+file.ownerUserName);
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
    public boolean delete (String filename, String username, String token) {
        if (um.isLogged(username, token)) {
            for (FileAtRepository f : repository) {
                if (f.ownerUserName.equals(username) && f.name.equals(filename)) {
                    try{
                        File file = new File(username+"/"+f.name);
                        if (file.delete())
                            System.out.println("File Deleted");
                        else
                            System.out.println("File Delation Fail");
                        file = new File(username+"/"+f.name+".info");
                        if (file.delete())
                            System.out.println("File Deleted");
                        else
                            System.out.println("File Delation Fail");
                    }
                    catch (Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
}
