import java.util.ArrayList;

public class SampleTxs {
   private Transaction[] txs;
   private SampleKeyPairs sampleKeyPairs;
   private ArrayList<RSAKeyPairHelper> people;
   private SampleUTXOPool sampleUPool;
   private UTXOPool uPool;
   private ArrayList<UTXO> uSet;

   public SampleTxs(SampleKeyPairs skp, SampleUTXOPool suPool) {
      sampleKeyPairs = skp;
      people = skp.getPeople();
      sampleUPool = suPool;
      uPool = suPool.getPool();
      uSet = uPool.getAllUTXO();
   }

   public UTXOPool getPool() {
      return uPool;
   }

   // all transactions are simple and valid
   public Transaction[] generateSimpleValid(int N, int maxI, int maxO, double maxTxFee) {
      if (N*maxI > uSet.size())
         throw new IllegalArgumentException("Required simple valid "
               + "transactions more than number of utxo's");
      if (maxTxFee < 0)
         throw new IllegalArgumentException("Fees should be greater than equal to zero");
      txs = new Transaction[N];
      int uC = 0;
      for (int i = 0; i < N; i++) {
         Transaction tx = new Transaction();
         int mI = SampleRandom.randomInt(maxI) + 1;
         double totalInput = 0;
         int keyMapping[] = new int[maxI];
         for (int j = 0; j < mI; j++) {
            UTXO ut = uSet.get(uC++);
            tx.addInput(ut.getTxHash(), ut.getIndex());
            totalInput += uPool.getTxOutput(ut).value;
            keyMapping[j] = sampleUPool.getPerson(ut);
         }
         int mO = SampleRandom.randomInt(maxO) + 1;
         double fee = SampleRandom.randomDouble(maxTxFee);
         double totalOutput = totalInput - fee;
         if (totalOutput < 0)
            totalOutput = 0;
         for (int j = 0; j < mO; j++) {
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = totalOutput/mO;
            tx.addOutput(value, addr);
         }
         for (int j = 0; j < mI; j++) {
            RSAKey priv = people.get(keyMapping[j]).getPrivateKey();
            byte[] sig = priv.sign(tx.getRawDataToSign(j));
            tx.addSignature(sig, j);
         }
         tx.finalize();
         txs[i] = tx;
      }
      return txs;
   }

   // all transactions are simple but some can be invalid because of signatures
   public Transaction[] generateSimpleInvalidSig(int N, int maxI, int maxO, double maxTxFee) {
      if (N*maxI > uSet.size())
         throw new IllegalArgumentException("Required simple valid "
               + "transactions more than number of utxo's");
      if (maxTxFee < 0)
         throw new IllegalArgumentException("Fees should be greater than equal to zero");
      txs = new Transaction[N];
      int uC = 0;
      for (int i = 0; i < N; i++) {
         Transaction tx = new Transaction();
         int mI = SampleRandom.randomInt(maxI) + 1;
         double totalInput = 0;
         int keyMapping[] = new int[maxI];
         for (int j = 0; j < mI; j++) {
            UTXO ut = uSet.get(uC++);
            tx.addInput(ut.getTxHash(), ut.getIndex());
            totalInput += uPool.getTxOutput(ut).value;
            // making first person sign all of them
            keyMapping[j] = 0;
         }
         int mO = SampleRandom.randomInt(maxO) + 1;
         double fee = SampleRandom.randomDouble(maxTxFee);
         double totalOutput = totalInput - fee;
         if (totalOutput < 0)
            totalOutput = 0;
         for (int j = 0; j < mO; j++) {
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = totalOutput/mO;
            tx.addOutput(value, addr);
         }
         for (int j = 0; j < mI; j++) {
            RSAKey priv = people.get(keyMapping[j]).getPrivateKey();
            byte[] sig = priv.sign(tx.getRawDataToSign(j));
            tx.addSignature(sig, j);
         }
         tx.finalize();
         txs[i] = tx;
      }
      return txs;
   }

   // all transactions are simple but some can be invalid because of inputSum < outputSum
   public Transaction[] generateSimpleInvalidValue(int N, int maxI, int maxO, double maxTxFee) {
      if (N*maxI > uSet.size())
         throw new IllegalArgumentException("Required simple valid "
               + "transactions more than number of utxo's");
      if (maxTxFee < 0)
         throw new IllegalArgumentException("Fees should be greater than equal to zero");
      txs = new Transaction[N];
      int uC = 0;
      for (int i = 0; i < N; i++) {
         Transaction tx = new Transaction();
         int mI = SampleRandom.randomInt(maxI) + 1;
         double totalInput = 0;
         int keyMapping[] = new int[maxI];
         for (int j = 0; j < mI; j++) {
            UTXO ut = uSet.get(uC++);
            tx.addInput(ut.getTxHash(), ut.getIndex());
            totalInput += uPool.getTxOutput(ut).value;
            keyMapping[j] = sampleUPool.getPerson(ut);
         }
         int mO = SampleRandom.randomInt(maxO) + 1;
         double fee = SampleRandom.randomDouble(maxTxFee);
         double totalOutput = 0;
         // making wrong with 1/2 probability
         if (fee <= maxTxFee/2)
            totalOutput = totalInput - fee;
         else
            totalOutput = totalInput + fee - maxTxFee/2;
         if (totalOutput < 0)
            totalOutput = 0;
         for (int j = 0; j < mO; j++) {
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = totalOutput/mO;
            tx.addOutput(value, addr);
         }
         for (int j = 0; j < mI; j++) {
            RSAKey priv = people.get(keyMapping[j]).getPrivateKey();
            byte[] sig = priv.sign(tx.getRawDataToSign(j));
            tx.addSignature(sig, j);
         }
         tx.finalize();
         txs[i] = tx;
      }
      return txs;
   }

   // some transactions have double spends on utxo's
   public Transaction[] generateSimpleValidDoubleSpends(int N, int maxI, int maxO, double maxTxFee) {
      if (N*maxI > uSet.size())
         throw new IllegalArgumentException("Required simple valid "
               + "transactions more than number of utxo's");
      if (maxTxFee < 0)
         throw new IllegalArgumentException("Fees should be greater than equal to zero");
      txs = new Transaction[N];
      int uC = 0;
      for (int i = 0; i < N; i++) {
         Transaction tx = new Transaction();
         int mI = SampleRandom.randomInt(maxI) + 1;
         double totalInput = 0;
         int keyMapping[] = new int[maxI];
         for (int j = 0; j < mI; j++) {
            UTXO ut = uSet.get(uC++);
            // the first input uses a UTXO from previous transaction's last input
            if (j == 0 && uC > 1) {
               uC -= 2;
               ut = uSet.get(uC++);
            }
            tx.addInput(ut.getTxHash(), ut.getIndex());
            totalInput += uPool.getTxOutput(ut).value;
            keyMapping[j] = sampleUPool.getPerson(ut);
         }
         int mO = SampleRandom.randomInt(maxO) + 1;
         double fee = SampleRandom.randomDouble(maxTxFee);
         double totalOutput = totalInput - fee;
         if (totalOutput < 0)
            totalOutput = 0;
         for (int j = 0; j < mO; j++) {
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = totalOutput/mO;
            tx.addOutput(value, addr);
         }
         for (int j = 0; j < mI; j++) {
            RSAKey priv = people.get(keyMapping[j]).getPrivateKey();
            byte[] sig = priv.sign(tx.getRawDataToSign(j));
            tx.addSignature(sig, j);
         }
         tx.finalize();
         txs[i] = tx;
      }
      return txs;
   }

   // all transactions are valid, some can be simple, some depend on other transactions
   public Transaction[] generateValidDependent(int N, int maxI, int maxO, double maxTxFee, double pUTXO) {
      if (N*maxI > uSet.size())
         throw new IllegalArgumentException("Required simple valid "
               + "transactions more than number of utxo's");
      if (maxTxFee < 0)
         throw new IllegalArgumentException("Fees should be greater than equal to zero");
      txs = new Transaction[N];
      int uC = 0;
      int keyMapping[] = new int[maxI];
      for (int i = 0; i < N; i++) {
         Transaction tx = new Transaction();
         int mI = SampleRandom.randomInt(maxI) + 1;
         double totalInput = 0;
         for (int j = 0; j < mI; j++) {
            if (i > 0) {
               double p = SampleRandom.randomDouble(1);
               if (p > pUTXO) {
                  // take input from some previous occuring transaction
                  Transaction rTx = txs[SampleRandom.randomInt(i)];
                  int rOp = SampleRandom.randomInt(rTx.numOutputs());
                  tx.addInput(rTx.getHash(), rOp);
                  totalInput += rTx.getOutput(rOp).value;
                  keyMapping[j] = sampleKeyPairs.getPerson(rTx.getOutput(rOp).address);
                  continue;
               }
            }
            UTXO ut = uSet.get(uC++);
            tx.addInput(ut.getTxHash(), ut.getIndex());
            totalInput += uPool.getTxOutput(ut).value;
            keyMapping[j] = sampleUPool.getPerson(ut);
         }
         int mO = SampleRandom.randomInt(maxO) + 1;
         double fee = SampleRandom.randomDouble(maxTxFee);
         double totalOutput = totalInput - fee;
         if (totalOutput < 0)
            totalOutput = 0;
         for (int j = 0; j < mO; j++) {
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = totalOutput/mO;
            tx.addOutput(value, addr);
         }
         for (int j = 0; j < mI; j++) {
            RSAKey priv = people.get(keyMapping[j]).getPrivateKey();
            byte[] sig = priv.sign(tx.getRawDataToSign(j));
            tx.addSignature(sig, j);
         }
         tx.finalize();
         txs[i] = tx;
      }
      return txs;
   }

   // all transactions are valid and simple except some which take inputs from non-existing utxo's
   public Transaction[] generateNonExisiting(int N, int maxI, int maxO, double maxTxFee, double pUTXO) {
      if (N*maxI > uSet.size())
         throw new IllegalArgumentException("Required simple valid "
               + "transactions more than number of utxo's");
      if (maxTxFee < 0)
         throw new IllegalArgumentException("Fees should be greater than equal to zero");
      txs = new Transaction[N];
      int uC = 0;
      int keyMapping[] = new int[maxI];
      for (int i = 0; i < N; i++) {
         Transaction tx = new Transaction();
         int mI = SampleRandom.randomInt(maxI) + 1;
         double totalInput = 0;
         for (int j = 0; j < mI; j++) {
            if (i > 0) {
               double p = SampleRandom.randomDouble(1);
               if (p > pUTXO) {
                  // take input from some random transaction
                  tx.addInput(new byte[256/8], 0);
                  keyMapping[j] = 0;
                  continue;
               }
            }
            UTXO ut = uSet.get(uC++);
            tx.addInput(ut.getTxHash(), ut.getIndex());
            totalInput += uPool.getTxOutput(ut).value;
            keyMapping[j] = sampleUPool.getPerson(ut);
         }
         int mO = SampleRandom.randomInt(maxO) + 1;
         double fee = SampleRandom.randomDouble(maxTxFee);
         double totalOutput = totalInput - fee;
         if (totalOutput < 0)
            totalOutput = 0;
         for (int j = 0; j < mO; j++) {
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = totalOutput/mO;
            tx.addOutput(value, addr);
         }
         for (int j = 0; j < mI; j++) {
            RSAKey priv = people.get(keyMapping[j]).getPrivateKey();
            byte[] sig = priv.sign(tx.getRawDataToSign(j));
            tx.addSignature(sig, j);
         }
         tx.finalize();
         txs[i] = tx;
      }
      return txs;
   }

   // all transactions are valid, some can be simple, some depend on other transactions, also double spends
   // also invalid amount sometime
   public Transaction[] generateComplex(int N, int maxI, int maxO, double maxTxFee, 
         double pUTXO, double pWrongValue) {
      if (maxTxFee < 0)
         throw new IllegalArgumentException("Fees should be greater than equal to zero");
      txs = new Transaction[N];
      int keyMapping[] = new int[maxI];
      for (int i = 0; i < N; i++) {
         Transaction tx = new Transaction();
         int mI = SampleRandom.randomInt(maxI) + 1;
         double totalInput = 0;
         for (int j = 0; j < mI; j++) {
            if (i > 0) {
               double p = SampleRandom.randomDouble(1);
               if (p > pUTXO) {
                  // take input from some previous occuring transaction
                  Transaction rTx = txs[SampleRandom.randomInt(i)];
                  int rOp = SampleRandom.randomInt(rTx.numOutputs());
                  tx.addInput(rTx.getHash(), rOp);
                  totalInput += rTx.getOutput(rOp).value;
                  keyMapping[j] = sampleKeyPairs.getPerson(rTx.getOutput(rOp).address);
                  continue;
               }
            }
            // taking a random utxo as input from the initial N utxo's
            // will result in a lot of double spends
            UTXO ut = uSet.get(SampleRandom.randomInt(N));
            tx.addInput(ut.getTxHash(), ut.getIndex());
            totalInput += uPool.getTxOutput(ut).value;
            keyMapping[j] = sampleUPool.getPerson(ut);
         }
         int mO = SampleRandom.randomInt(maxO) + 1;
         double fee = SampleRandom.randomDouble(maxTxFee);
         double totalOutput = 0;
         if (fee <= pWrongValue*maxTxFee)
            totalOutput = totalInput + fee;
         else
            totalOutput = totalInput - fee;
         if (totalOutput < 0)
            totalOutput = 0;
         for (int j = 0; j < mO; j++) {
            int rIndex = SampleRandom.randomInt(people.size());
            RSAKey addr = people.get(rIndex).getPublicKey();
            double value = totalOutput/mO;
            tx.addOutput(value, addr);
         }
         for (int j = 0; j < mI; j++) {
            RSAKey priv = people.get(keyMapping[j]).getPrivateKey();
            byte[] sig = priv.sign(tx.getRawDataToSign(j));
            tx.addSignature(sig, j);
         }
         tx.finalize();
         txs[i] = tx;
      }
      return txs;
   }
}
