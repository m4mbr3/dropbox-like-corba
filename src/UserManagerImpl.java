package src;

import java.util.*;
import org.omg.CORBA.*;
import Dropboxlike.*;
import java.security.MessageDigest;

public class UserManagerImpl extends UserManagerPOA {
    ArrayList<Logged> logged_user;
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
        for (Logged el : logged_user) {
            if (el.dev.username.equals(username) && el.token.equals(token)) {
                return true;
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
    public String login(String username, String password, String dev_id) {
        for (UserInfo el : users) {
            System.out.println(el.password+"\n"+SHAchecksumpassword(password));
            if(el.username.compareTo(username) == 0 && el.password.compareTo(SHAchecksumpassword(password)) == 0) {
                for (Logged log : logged_user) {
                    if(log.dev.dev_id.compareTo(dev_id) == 0 && log.dev.username.compareTo(username) == 0) {
                        return "ALREADY_LOGGED";
                    }
                }
                String sha = SHAchecksumpassword(new Integer(new Random().nextInt()).toString());
                logged_user.add(new Logged(new Device(username, dev_id),sha));
                return sha.toString();
            }
        }
        return "ERROR_NO_USER";
    }
    public boolean logout(String username, String device) {
        for (Logged el : logged_user) {
            if (el.dev.username.equals(username) && el.dev.dev_id.equals(device)) {
                logged_user.remove(el);
                return true;
            }
        }
        return false;
    }
    public boolean remove (String username, String password) {
        for (UserInfo el : users) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                byte[] password_bytes = password.getBytes();
                md.update(password_bytes);
                if(el.username.equals(username) && el.password.equals(md.digest().toString())) {
                    users.remove(el);
                    for (Logged e: logged_user) {
                        if (e.dev.username.equals(username)){
                            logged_user.remove(e);
                        }
                    }
                    return true;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
