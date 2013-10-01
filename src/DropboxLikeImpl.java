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
    public FileAtRepository[] askListUser (String username, String token) {
        //TODO complete this function
        FileAtRepository[] repo = null;
        return repo;
    }
    public boolean delete (String filename, String username, String token) {
        //TODO complete this function


        return false;
    }
}
