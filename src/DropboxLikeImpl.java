package src;

import java.util.*;
import Dropboxlike.*;
import org.omg.CORBA.*;

public class DropboxLikeImpl extends RepositoryPOA {
    private ArrayList<FileAtRepository> repository;
    UserManagerImpl um;
    DropboxLikeImpl() {
        repository =  new ArrayList<FileAtRepository>();
        um = new UserManagerImpl();
    }
    public boolean send(FileAtRepository file, String username, String token ) {
        //if( um.islogged(username))
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
    public FileAtRepository[] fileList(){
        //TODO complete this function
        FileAtRepository[] repo = null;
        return repo;
    }
}
