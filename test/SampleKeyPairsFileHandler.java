import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class SampleKeyPairsFileHandler {
   public static void writeKeyPairsToFile(SampleKeyPairs skp, String filename) 
         throws FileNotFoundException, IOException {
      FileOutputStream fos = new FileOutputStream(filename);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      ArrayList<RSAKeyPairHelper> people = skp.getPeople();
      int n = people.size();
      oos.writeInt(n);
      for (int i = 0; i < n; i++) {
         RSAKey privKey = people.get(i).getPrivateKey();
         RSAKey pubKey = people.get(i).getPublicKey();

         BigInteger[] priv = new BigInteger[2];
         priv[0] = privKey.getExponent();
         priv[1] = privKey.getModulus();

         BigInteger[] pub = new BigInteger[2];
         pub[0] = pubKey.getExponent();
         pub[1] = pubKey.getModulus();

         int index = skp.getPerson(pubKey);

         oos.writeObject(pub);
         oos.writeObject(priv);
         oos.writeInt(index);
      }
      oos.close();
      fos.close();
   }

   public static SampleKeyPairs readKeyPairsFromFile(String filename) 
         throws FileNotFoundException, IOException {
      // Read an RSAKey from a file, return the key that was read
      FileInputStream fis = new FileInputStream(filename);
      ObjectInputStream ois = new ObjectInputStream(fis);
      try {
         ArrayList<RSAKeyPairHelper> people = 
               new ArrayList<RSAKeyPairHelper>();
         HashMap<RSAPublicKeyStub, Integer> pubKeyMapping = new HashMap<RSAPublicKeyStub, Integer>();
         int n = ois.readInt();
         for (int i = 0; i < n; i++) {
            BigInteger[] pub = (BigInteger[]) ois.readObject();
            BigInteger[] priv = (BigInteger[]) ois.readObject();
            int index = ois.readInt();
            RSAKey privKey = new RSAKey(priv[0], priv[1]);
            RSAKey pubKey = new RSAKey(pub[0], pub[1]);
            people.add(new RSAKeyPairHelper(pubKey, privKey));
            pubKeyMapping.put(new RSAPublicKeyStub(pubKey.getExponent(), pubKey.getModulus()), 
                  index);
         }
         SampleKeyPairs skp = new SampleKeyPairs(people, pubKeyMapping);
         ois.close();
         fis.close();
         return skp;
      } catch(ClassNotFoundException x) {
         ois.close();
         fis.close();
         return null;
      }
   }
   
   public static void main(String args[]) throws FileNotFoundException, IOException {
      System.out.println("Generating rsa key pairs for dropbox test");
      SampleKeyPairs skp = new SampleKeyPairs(15);
      writeKeyPairsToFile(skp, "DropboxSampleKeyPairs.txt");
      
      System.out.println("Generating rsa key pairs for dropbox test for maximum fees transactions");
      SampleKeyPairs skpMaxFees = new SampleKeyPairs(5);
      writeKeyPairsToFile(skpMaxFees, "DropboxSampleMaxFeeKeyPairs.txt");
   }
}
