import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

public class SampleUTXOPoolFileHandler {
   public static void writeUTXOPoolToFile(SampleUTXOPool sup, String filename) 
         throws FileNotFoundException, IOException {
      FileOutputStream fos = new FileOutputStream(filename);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      UTXOPool uPool = sup.getPool();
      ArrayList<UTXO> utxos = uPool.getAllUTXO();
      int n = utxos.size();
      oos.writeInt(n);
      for (int i = 0; i < n; i++) {
         UTXO ut = utxos.get(i);
         Transaction.Output op = uPool.getTxOutput(ut);
         oos.writeObject(ut.getTxHash());
         oos.writeInt(ut.getIndex());
         oos.writeDouble(op.value);

         RSAKey pubKey = op.address;
         BigInteger[] pub = new BigInteger[2];
         pub[0] = pubKey.getExponent();
         pub[1] = pubKey.getModulus();
         oos.writeObject(pub);
      }
      oos.close();
      fos.close();
   }

   public static SampleUTXOPool readSampleUTXOPoolFromFile(SampleKeyPairs skp, String filename) 
         throws FileNotFoundException, IOException {
      FileInputStream fis = new FileInputStream(filename);
      ObjectInputStream ois = new ObjectInputStream(fis);
      try {
         UTXOPool uPool = new UTXOPool();
         int n = ois.readInt();
         for (int i = 0; i < n; i++) {
            byte[] hash = (byte[]) ois.readObject();
            int index = ois.readInt();
            double value = ois.readDouble();

            BigInteger[] pub = (BigInteger[]) ois.readObject();
            RSAKey pubKey = new RSAKey(pub[0], pub[1]);
            
            UTXO ut = new UTXO(hash, index);
            Transaction tx = new Transaction();
            tx.addOutput(value, pubKey);
            uPool.addUTXO(ut, tx.getOutput(0));
         }
         SampleUTXOPool sup = new SampleUTXOPool(uPool, skp);
         ois.close();
         fis.close();
         return sup;
      } catch(ClassNotFoundException x) {
         ois.close();
         fis.close();
         return null;
      }
   }

   public static void main(String args[]) throws FileNotFoundException, IOException {
      System.out.println("Generating sample utxo pool for dropbox test");
      SampleKeyPairs skp = SampleKeyPairsFileHandler.readKeyPairsFromFile("DropboxSampleKeyPairs.txt");
      SampleUTXOPool sup = new SampleUTXOPool(skp, 1000, 5, 0.1);
      writeUTXOPoolToFile(sup, "DropboxSampleUTXOPool.txt");

      System.out.println("Generating sample utxo pool for dropbox test for maximum fees transactions");
      SampleKeyPairs skpMaxFees = SampleKeyPairsFileHandler.readKeyPairsFromFile("DropboxSampleMaxFeeKeyPairs.txt");
      SampleUTXOPool supMaxFees = new SampleUTXOPool(skpMaxFees, 100, 5, 0.1);
      writeUTXOPoolToFile(supMaxFees, "DropboxSampleMaxFeeUTXOPool.txt");
   }
}
