import java.util.ArrayList;

public class SampleUTXOPool {
   private UTXOPool uPool;
   private SampleKeyPairs sampleKeyPairs;
   private ArrayList<RSAKeyPairHelper> people;

   public SampleUTXOPool(SampleKeyPairs skp, int N, int maxOut, double maxValue) {
      sampleKeyPairs = skp;
      people = skp.getPeople();
      generate(N, maxOut, maxValue);
   }
   
   public SampleUTXOPool(UTXOPool utxoPool, SampleKeyPairs skp) {
      uPool = utxoPool;
      sampleKeyPairs = skp;
      people = skp.getPeople();
   }

   // generate N transactions, each having a maximum of k outputs
   private void generate(int N, int maxOut, double maxValue) {
      uPool = new UTXOPool();
      for (int i = 0; i < N; i++) {
         int num = SampleRandom.randomInt(maxOut) + 1;
         Transaction tx = new Transaction();
         tx.addInput(null, 0);
         for (int j = 0; j < num; j++) {
            // pick a random public address
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = SampleRandom.randomDouble(maxValue);
            tx.addOutput(value, addr);
         }
         tx.finalize();
         // add all tx outputs to utxo pool
         for (int j = 0; j < num; j++) {
            UTXO ut = new UTXO(tx.getHash(), j);
            uPool.addUTXO(ut, tx.getOutput(j));
         }
      }
   }

   public UTXOPool getPool() {
      return uPool;
   }
   
   public SampleKeyPairs getSampleKeyPairs() {
      return sampleKeyPairs;
   }

   public int getPerson(UTXO ut) {
      Transaction.Output op = uPool.getTxOutput(ut);
      if (op == null) {
         System.out.println("output is null");
         return -1;
      }
      return sampleKeyPairs.getPerson(op.address);
   }
}
