
public class TxHandlerVerifier {
   private ReferenceTxHandler rTxHandler;
   
   public TxHandlerVerifier(UTXOPool pool) {
      rTxHandler = new ReferenceTxHandler(pool);
   }
   
   private boolean checkAllSatisfied(Transaction[] stTxs) {
      Transaction[] rTxs = rTxHandler.handleTxs(stTxs);
      if (rTxs.length == stTxs.length)
         return true;
      else {
         System.out.println("All transactions returned are not satisfied/valid");
         return false;
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
      if (!checkAllSatisfied(stTxs))
         return false;
      Transaction[] r2 = rTxHandler.handleTxs(allTxs);
      if (r2.length > 0) {
         System.out.println("Returned set is not a maximal set of transactions");
         return false;
      }
      else
         return true;
   }
}
