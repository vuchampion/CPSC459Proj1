import java.util.ArrayList;

public class MaxFeeTxHandlerVerifier {
   private ReferenceMaxFeeTxHandler rMaxFeesTxHandler1, rMaxFeesTxHandler2;

   public MaxFeeTxHandlerVerifier(UTXOPool pool) {
      rMaxFeesTxHandler1 = new ReferenceMaxFeeTxHandler(pool);
      rMaxFeesTxHandler2 = new ReferenceMaxFeeTxHandler(pool);
   }

   private double checkAllSatisfied(Transaction[] stTxs) {
      ReferenceMaxFeeTxHandler.TransactionFees rTxs = 
            rMaxFeesTxHandler1.handleTxs(stTxs);
      if (rTxs == null) {
         if (stTxs != null && stTxs.length > 0) {
            System.out.println("All transactions returned are not satisfied/valid");
            return -1;
         } else {
            return 0;
         }
      }
      if (rTxs.txs.size() == stTxs.length)
         return rTxs.fees;
      else {
         System.out.println("All transactions returned are not satisfied/valid");
         return -1;
      }
   }

   public boolean check(Transaction[] allTx, Transaction[] stTx) {
      Transaction[] allTxs = allTx;
      int stTxSize = 0;
      for (int i = 0; i < stTx.length; i++) {
         if (stTx[i] != null)
            stTxSize++;
      }
      Transaction[] stTxs = new Transaction[stTxSize];
      int j = 0;
      for (int i = 0; i < stTx.length; i++) {
         if (stTx[i] != null)
            stTxs[j++] = stTx[i];
      }
      double fees = checkAllSatisfied(stTxs);
      if (fees < 0)
         return false;
      ReferenceMaxFeeTxHandler.TransactionFees rTxs = 
            rMaxFeesTxHandler2.handleTxs(allTxs);
      double maxFees = 0;
      if (rTxs != null)
         maxFees = rTxs.fees;
      if (fees < maxFees) {
         System.out.println("Returned set is not a maximum fees set of transactions");
         System.out.println("Returned fees " + fees + ", maximum fees " + maxFees);
         return false;
      }
      else {
         System.out.println("Correct, Returned fees " + fees + " = maximum fees ");
         return true;
      }
   }
}
