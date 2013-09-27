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
    public boolean isLogged(String username, String token) {
        for (Logged el : logged_user) {
            if (el.dev.username.equals(username) && el.token.equals(token)) {
                return true;
            }
        }
        return false;
    }
    public boolean subscribe(String name, String surname, String username, String password) {
        for(Logged el : logged_user) {
            if(el.dev.username.equals(username)){
                return false;
            }
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] password_bytes = password.getBytes();
            md.update(password_bytes);
            users.add(new UserInfo(name, surname, username, md.digest().toString()  ));
        }
        catch (Exception cnse) {
            cnse.printStackTrace();
        }
        return true;
    }
    public String login(String username, String password, String dev_id) {
        for (UserInfo el : users) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                byte[] password_bytes = password.getBytes();
                md.update(password_bytes);
                if(el.username.equals(username) && el.password.equals(md.digest().toString())) {
                    md.update(new Integer(new Random().nextInt()).toString().getBytes());
                    logged_user.add(new Logged(new Device(username, dev_id), md.digest().toString()));
                    return md.digest().toString();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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
}
