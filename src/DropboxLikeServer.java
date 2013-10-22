package src;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import Dropboxlike.*;
import java.io.*;

public class DropboxLikeServer {
    public static void main(String[] args) {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get a reference to rootpoa & activate the POA Manager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            DropboxLikeImpl dropbox = new DropboxLikeImpl();
            String home_env = System.getenv("DROPBOXLIKE_HOME");
            if (home_env != null)
                dropbox.set_home(home_env +"/dropboxlike");
            else
                dropbox.set_home("dropboxlike");
            File home_dir = new File(dropbox.get_home());
            home_dir.mkdirs();
            dropbox.setORB(orb);
            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(dropbox);
            Repository href = RepositoryHelper.narrow(ref);
            // get the root naming context
            // NameService invokes the name service

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            String name = "DBServer";
            NameComponent path[] = ncRef.to_name(name);
            ncRef.rebind(path, href);
            //server bootstrap
            dropbox.bootstrap();
            // wait for invocations from clients
            orb.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
