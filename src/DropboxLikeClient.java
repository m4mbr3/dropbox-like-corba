package src;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import Dropboxlike.*;
import java.util.*;
import java.io.*;
import java.io.DataInputStream;
import java.security.MessageDigest;

public class DropboxLikeClient {
    static Repository dropboxImpl;
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

    public static void main(String[] args) {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            //  get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            String name = "DBServer";
            dropboxImpl = RepositoryHelper.narrow(ncRef.resolve_str(name));
            char c='r';
            File fis = new File("/home/m4mbr3/documents/bording_pass/rodi/BoardingPass.pdf");
            byte[] fileData = new byte[(int) fis.length()];
            DataInputStream dis = new DataInputStream((new FileInputStream(fis)));
            dis.readFully(fileData);
            dis.close();
            FileAtRepository entity = new FileAtRepository();
            entity.ownerUserName = "mambro";
            entity.md5 = SHAchecksumfile("/home/m4mbr3/documents/bording_pass/rodi/BoardingPass.pdf");
            entity.cont = fileData;
            entity.name = "BoardingPass.pdf";
            dropboxImpl.subscribe("Andrea","Mambretti","mambro", "my_passwd");
            String token = dropboxImpl.login("mambro", "my_passwd","EE:EE:EE:EE:EE:EE");
            while (c != 'e') {
                System.out.println(dropboxImpl.send(entity, "mambro", token ));
                c = 'e';
            }
            //dropboxImpl.remove("mambro","my_passwd");
            dropboxImpl.logout("mambro","EE:EE:EE:EE:EE:EE");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
