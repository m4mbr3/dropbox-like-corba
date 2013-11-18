package src;

import java.util.*;
import org.omg.CORBA.*;
import java.lang.Object;
import Dropboxlike.*;
import java.security.MessageDigest;

public class UserManagerImpl {
    ArrayList<Logged> logged_u= new ArrayList<Logged>();
    List logged_user = Collections.synchronizedList(logged_u);
    ArrayList<UserInfo> users;

    UserManagerImpl() {
        logged_user = new ArrayList<Logged>();
        users = new ArrayList<UserInfo>();
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

    public boolean isLogged(String username, String token) {
        synchronized(logged_user) {
            for (Object el : logged_user) {
                if (((Logged)el).dev.username.compareTo(username) == 0 && ((Logged)el).token.compareTo(token) == 0) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean subscribe(String name, String surname, String username, String password) {
        for(UserInfo el : users) {
            if(el.username.compareTo(username) == 0){
                return false;
            }
        }
        UserInfo element = new UserInfo(name,surname, username, SHAchecksumpassword(password));
        users.add(element);
        return true;
    }
    public boolean record (String name, String surname, String username, String password_sha) {
        for(UserInfo el : users) {
            if (el.username.compareTo(username) == 0) {
                return false;
            }
        }
        UserInfo element =  new UserInfo(name, surname, username, password_sha);
        users.add(element);
        return true;
    }

    public String login(String username, String password, String dev_id) {
        for (UserInfo el : users) {
            if(el.username.compareTo(username) == 0 && el.password.compareTo(SHAchecksumpassword(password)) == 0) {
                synchronized(logged_user){
                    for (Object log : logged_user) {
                        if(((Logged)log).dev.dev_id.compareTo(dev_id) == 0 && ((Logged)log).dev.username.compareTo(username) == 0) {
                            //To do not have  ghost logged user, the program receives the previous session token to the user.
                            return ((Logged)log).token;
                        }
                    }
                }
                String sha = SHAchecksumpassword(new Integer(new Random().nextInt()).toString());
                logged_user.add(new Logged(new Device(username, dev_id),sha));
                return sha.toString();
            }
        }
        return "INVALID_USER";
    }

    public boolean logout(String username, String device, String token) {
        synchronized(logged_user) {
            Logged to_rm=null;
            for (Object el : logged_user) {
                if (((Logged)el).dev.username.compareTo(username) == 0 && ((Logged)el).dev.dev_id.compareTo(device) == 0 && ((Logged)el).token.compareTo(token) == 0 ) {
                    to_rm = ((Logged) el);
                }
            }
            if (to_rm != null) {
                logged_user.remove(to_rm);
                return true;
            }
            return false;
        }
    }

    public boolean check_username(String username) {
        for (UserInfo s : users) {
            if (s.username.compareTo(username) == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean remove (String username, String password) {
        for (UserInfo el : users) {
            if(el.username.compareTo(username) == 0 && el.password.compareTo(SHAchecksumpassword(password)) == 0) {
                users.remove(el);
                ArrayList<Logged> my_ele = new ArrayList<Logged>();
                synchronized(logged_user) {
                    for (Object e : logged_user) {
                        if (((Logged)e).dev.username.compareTo(username) == 0){
                            my_ele.add((Logged)e);
                        }
                    }
                    for (Logged e : my_ele) {
                        logged_user.remove(e);
                    }
                }
                return true;
            }
        }
        return false;
    }
}
