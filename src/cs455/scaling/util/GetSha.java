package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GetSha {
    public static String SHA1FromBytes(byte[] data) {
        try {
            final MessageDigest md=MessageDigest.getInstance("SHA-1");
            md.update(data,0,data.length);
            byte[] bytes=md.digest();
            return String.format("%0" + (bytes.length << 1) + "x",new BigInteger(1,bytes));
        }
        catch (  final NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static String sha1Hex(final byte[] data){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] hash = digest.digest(data);
            BigInteger hashInt = new BigInteger(1, hash);
            String hashString = hashInt.toString(16);
            if(hashString.length() == 39) {
                hashString = "0" + hashString;
            }
            return hashString;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }

//        try {
//            final MessageDigest md=MessageDigest.getInstance("SHA-1");
//            md.update(data,0,data.length);
//            byte[] bytes=md.digest();
//            return String.format("%0" + (bytes.length << 1) + "x",new BigInteger(1,bytes));
//        }
//        catch (  final NoSuchAlgorithmException e) {
//            throw new UnsupportedOperationException(e);
//        }
    }
}
