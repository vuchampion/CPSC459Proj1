import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

public class TransactionsArrayFileHandler {
   public static void writeTransactionsToFile(Transaction[] txs, String filename) 
         throws FileNotFoundException, IOException {
      FileOutputStream fos = new FileOutputStream(filename);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      int n = txs.length;
      oos.writeInt(n);
      for (int i = 0; i < n; i++) {
         // write each transaction here
         Transaction tx = txs[i];

         // tx hash
         oos.writeObject(tx.getHash());
         
         // inputs
         ArrayList<Transaction.Input> inputs = tx.getInputs();
         oos.writeInt(inputs.size());
         for (int j = 0; j < inputs.size(); j++) {
            Transaction.Input in = inputs.get(j);
            oos.writeObject(in.prevTxHash);
            oos.writeInt(in.outputIndex);
            oos.writeObject(in.signature);
         }

         // outputs
         ArrayList<Transaction.Output> outputs = tx.getOutputs();
         oos.writeInt(outputs.size());
         for (int j = 0; j < outputs.size(); j++) {
            Transaction.Output op = outputs.get(j);
            oos.writeDouble(op.value);
            RSAKey pubKey = op.address;
            BigInteger[] pub = new BigInteger[2];
            pub[0] = pubKey.getExponent();
            pub[1] = pubKey.getModulus();
            oos.writeObject(pub);
         }
      }
      oos.close();
      fos.close();
   }

   public static Transaction[] readTransactionsFromFile(String filename) 
         throws FileNotFoundException, IOException {
      FileInputStream fis = new FileInputStream(filename);
      ObjectInputStream ois = new ObjectInputStream(fis);
      try {
         int n = ois.readInt();
         Transaction[] txs = new Transaction[n];
         for (int i = 0; i < n; i++) {
            Transaction tx = new Transaction();
            
            // hash
            byte[] hash = (byte[]) ois.readObject();
            tx.setHash(hash);
            
            // inputs
            int inputsSize = ois.readInt();
            for (int j = 0; j < inputsSize; j++) {
               byte[] prevTxHash = (byte[]) ois.readObject();
               int outputIndex = ois.readInt();
               byte[] signature = (byte[]) ois.readObject();
               tx.addInput(prevTxHash, outputIndex);
               tx.addSignature(signature, j);
            }
            
            // outputs
            int outputsSize = ois.readInt();
            for (int j = 0; j < outputsSize; j++) {
               double value = ois.readDouble();
               BigInteger[] pub = (BigInteger[]) ois.readObject();
               RSAKey address = new RSAKey(pub[0], pub[1]);
               tx.addOutput(value, address);
            }
            
            txs[i] = tx;
         }
         ois.close();
         fis.close();
         return txs;
      } catch(ClassNotFoundException x) {
         ois.close();
         fis.close();
         return null;
      }
   }
}
