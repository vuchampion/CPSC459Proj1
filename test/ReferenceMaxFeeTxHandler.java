import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class ReferenceMaxFeeTxHandler {
   private class PQData implements Comparable<PQData> {
      boolean reachability;
      int numDS;
      UTXO utxo;
      Transaction tx;

      public PQData(boolean r, UTXO ut, Transaction t) {
         reachability = r;
         numDS = 0;
         utxo = ut;
         tx = t;
      }

      public PQData(boolean r, int nD, UTXO ut, Transaction t) {
         reachability = r;
         numDS = nD;
         utxo = ut;
         tx = t;
      }

      public int compareTo(PQData p) {
         boolean reach = p.reachability;
         int numD = p.numDS;
         UTXO ut = p.utxo;
         if (reachability && !reach)
            return -1;
         else if (!reachability && reach)
            return 1;
         else {
            if (numDS > numD)
               return 1;
            else if (numDS < numD)
               return -1;
            else
               return utxo.compareTo(ut);
         }
      } 

      private String byteString(byte[] a) {
         String s = "";
         for (int i = 0; i < a.length/4; i++) {
            s += a[i] + ",";
         }
         return s;
      }

      public String toString() {
         String s = "********";
         s += "Reachability is " + reachability + ", ";
         s += "Num DS is " + numDS + ", ";
         s += "UTXO index is " + utxo.getIndex() + ", ";
         s += "UTXO is " + byteString(utxo.getTxHash()) + "Tx is " + byteString(tx.getHash()) + "*********\n";
         return s;
      }
   }

   private class TXData {
      ArrayList<UTXO> inputs;
      ArrayList<UTXO> outputs;

      public TXData(Transaction tx) {
         inputs = new ArrayList<UTXO>();
         outputs = new ArrayList<UTXO>();
         for (int i = 0; i < tx.getInputs().size(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO uin = new UTXO(in.prevTxHash, in.outputIndex);
            inputs.add(uin);
         }
         for (int i = 0; i < tx.getOutputs().size(); i++) {
            UTXO uop = new UTXO(tx.getHash(), i);
            outputs.add(uop);
         }
      }
   }

   public class TransactionFees {
      public ArrayList<Transaction> txs;
      public double fees;
      public TransactionFees(ArrayList<Transaction> txs, double fees) {
         this.txs = txs;
         this.fees = fees;
      }
   }

   private static class PQDataComparator implements Comparator<PQData> {
      public int compare(PQData p1, PQData p2) {
         return p1.compareTo(p2);
      }
   }

   private static final Comparator<PQData> pqComparator = new PQDataComparator(); 

   private UTXOPool utxoPool;
   private UTXOPool totalUTXOPool;

   public ReferenceMaxFeeTxHandler(UTXOPool up) {
      utxoPool = new UTXOPool(up);
      totalUTXOPool = new UTXOPool(up);
   }

   // returns a negative value if invalid transaction or if some input is not in utxo pool
   public double getFees(Transaction tx) {
      double totalInput = 0;
      HashSet<UTXO> utxosSeen = new HashSet<UTXO>();
      for (int i = 0; i < tx.getInputs().size(); i++) {
         Transaction.Input in = tx.getInput(i);
         UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
         if (!utxosSeen.add(ut))
            return -1;
         Transaction.Output op = totalUTXOPool.getTxOutput(ut);
         if (op == null)
            return -1;
         RSAKey address = op.address;
         if (!address.verifySignature(tx.getRawDataToSign(i), in.signature))
            return -1;
         totalInput += op.value;
      }
      double totalOutput = 0;
      ArrayList<Transaction.Output> txOutputs = tx.getOutputs();
      for (Transaction.Output op : txOutputs) {
         if (op.value < 0)
            return -1;
         totalOutput += op.value;
      }
      return (totalInput - totalOutput);
   }

   public boolean isValidTx(Transaction tx) {
      return getFees(tx) >= 0;
   }

   private TransactionFees maxFeeTransactions(IndexMinPQ<PQData> PQ,
         PQData[] AS,
         HashMap<UTXO, ArrayList<Integer> > HP,
         HashMap<byte[], TXData> HT) {
      TransactionFees maxTx = null;
      ArrayList<PQData> topDS = new ArrayList<PQData>();
      ArrayList<Integer> topDSI = new ArrayList<Integer>();
      if (PQ.isEmpty())
         return null;
      PQData min = PQ.minKey();
      if (!min.reachability) 
         return null;
      int nD = min.numDS;
      for (int i = 0; i < nD; i++) {
         PQData p = PQ.minKey();
         int minI = PQ.delMin();
         topDSI.add(minI);
         topDS.add(p);
      }
      for (int i = 0; i < nD; i++) {
         PQData p = topDS.get(i);
         Transaction tx = p.tx;
         UTXO ut = p.utxo;
         // give this utxo to transaction tx
         Transaction.Output remOutput = utxoPool.getTxOutput(ut);
         utxoPool.removeUTXO(ut);

         ArrayList<UTXO> txInputs = HT.get(tx.getHash()).inputs;
         ArrayList<UTXO> txOutputs = HT.get(tx.getHash()).outputs;
         txInputs.remove(ut);
         if (txInputs.size() == 0 && getFees(tx) >= 0) {
            for (int j = 0; j < txOutputs.size(); j++) {
               UTXO uop = txOutputs.get(j);
               utxoPool.addUTXO(uop, tx.getOutput(j));
               totalUTXOPool.addUTXO(uop, tx.getOutput(j));
               ArrayList<Integer> dependingTx = HP.get(uop);
               if (dependingTx != null) {
                  for (int k = 0; k < dependingTx.size(); k++) {
                     int dependingIndex = dependingTx.get(k);
                     PQData pq = AS[dependingIndex];
                     PQData npq = new PQData(true, pq.numDS, pq.utxo, pq.tx);
                     PQ.changeKey(dependingIndex, npq);
                     AS[dependingIndex] = npq;
                  }
               }
            }
         }
         TransactionFees maxTemp = maxFeeTransactions(PQ, AS, HP, HT);
         if (txInputs.size() == 0) {
            double myFees = getFees(tx);
            if (myFees >= 0) {
               if (maxTemp == null) {
                  ArrayList<Transaction> txTemp = new ArrayList<Transaction>();
                  txTemp.add(tx);
                  maxTemp = new TransactionFees(txTemp, myFees);
               } else {
                  maxTemp.fees += myFees;
                  maxTemp.txs.add(tx);
               }

               for (int j = 0; j < txOutputs.size(); j++) {
                  UTXO uop = txOutputs.get(j);
                  utxoPool.removeUTXO(uop);
                  ArrayList<Integer> dependingTx = HP.get(uop);
                  if (dependingTx != null) {
                     for (int k = 0; k < dependingTx.size(); k++) {
                        int dependingIndex = dependingTx.get(k);
                        PQData pq = AS[dependingIndex];
                        PQData npq = new PQData(false, pq.numDS, pq.utxo, pq.tx);
                        PQ.changeKey(dependingIndex, npq);
                        AS[dependingIndex] = npq;
                     }
                  }
               }
            }
         }
         if (maxTemp != null && (maxTx == null || maxTx.fees < maxTemp.fees))
            maxTx = maxTemp;
         txInputs.add(ut);
         utxoPool.addUTXO(ut, remOutput);
      }
      for (int i = 0; i < nD; i++) {
         int minI = topDSI.get(i);
         PQData p = topDS.get(i);
         PQ.insert(minI, p);
      }
      return maxTx;
   }

   private void updatePool(Transaction[] txs) {
      totalUTXOPool = new UTXOPool(utxoPool);
      while(true) {
         boolean change = false;
         for (int i = 0; i < txs.length; i++) {
            if (getFees(txs[i]) >= 0) {
               Transaction tx = txs[i];
               ArrayList<Transaction.Input> inputs = tx.getInputs();
               ArrayList<Transaction.Output> outputs = tx.getOutputs();
               for (Transaction.Input in : inputs) {
                  totalUTXOPool.removeUTXO(new UTXO(in.prevTxHash, in.outputIndex));
               }
               for (int j = 0; j < outputs.size(); j++) {
                  totalUTXOPool.addUTXO(new UTXO(tx.getHash(), j), outputs.get(j));
               }
               change = true;
            }
         }
         if (!change)
            break;
      }
      utxoPool = new UTXOPool(totalUTXOPool);
   }

   // do not change utxo pool
   public TransactionFees handleTxs(Transaction[] allTx) {
      // making the array AS[] containing all information to into priority queue
      ArrayList<PQData> S = new ArrayList<PQData>();
      for (int i = 0; i < allTx.length; i++) {
         Transaction tx = allTx[i];
         for (int j = 0; j < tx.getInputs().size(); j++) {
            Transaction.Input in = tx.getInput(j);
            UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
            if (utxoPool.contains(ut)) {
               S.add(new PQData(true, ut, tx));
            } else {
               S.add(new PQData(false, ut, tx));
            }
         }
      }
      PQData[] AS = S.toArray(new PQData[0]);
      Arrays.sort(AS, pqComparator);
      for (int i = 0; i < AS.length; ) {
         int count = 1;
         int j = i+1;
         while (j < AS.length && AS[j].utxo.equals(AS[i].utxo)) { 
            count++;
            j++;
         }
         for (int k = i; k < j; k++)
            AS[k].numDS = count;
         i = j;
      }
      Arrays.sort(AS, pqComparator);

      // hash map to aid AS[]
      HashMap<UTXO, ArrayList<Integer> > HP = new HashMap<UTXO, ArrayList<Integer> >();
      for (int i = 0; i < AS.length; ) {
         ArrayList<Integer> aI = new ArrayList<Integer>();
         aI.add(i);
         int j = i + 1;
         while (j < AS.length && AS[j].utxo.equals(AS[i].utxo)) { 
            aI.add(j);
            j++;
         }
         HP.put(AS[i].utxo, aI);
         i = j;
      }

      // priority queue
      IndexMinPQ<PQData> PQ = new IndexMinPQ<PQData>(AS.length);
      for (int i = 0; i < AS.length; i++) {
         PQ.insert(i, AS[i]);
      }

      // hashmap from transaction hash and its inputs and outputs
      HashMap<byte[], TXData> HT = new HashMap<byte[], TXData>();
      for (int i = 0; i < allTx.length; i++) {
         HT.put(allTx[i].getHash(), new TXData(allTx[i]));
      }

      // algorithm
      TransactionFees maxTx = maxFeeTransactions(PQ, AS, HP, HT);
      if (maxTx == null)
         return null;
      ArrayList<Transaction> maxTxs =  maxTx.txs;
      Transaction[] txs = new Transaction[maxTxs.size()];
      int j = 0;
      for (int i = maxTxs.size()-1; i >= 0; i--)
         txs[j++] = maxTxs.get(i);
      updatePool(txs);
      return maxTx;
   }
}

