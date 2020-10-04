import java.io.FileNotFoundException;
import java.io.IOException;

public class SampleTxsFileHandler {
   public static void writeDropboxTest1(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 1: test handleTransactions() with simple and valid transactions");

      Transaction[] allTxs1 = sampleTxs.generateSimpleValid(2, 5, 5, 0.005);
      Transaction[] allTxs2 = sampleTxs.generateSimpleValid(50, 5, 5, 0.005);
      Transaction[] allTxs3 = sampleTxs.generateSimpleValid(100, 5, 5, 0.005);

      String common = "DropboxSampleTxsTest1-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }

   public static void writeDropboxTest2(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 2: test handleTransactions() with simple but "
            + "some invalid transactions because of invalid signatures");

      Transaction[] allTxs1 = sampleTxs.generateSimpleInvalidSig(2, 5, 5, 0.005);
      Transaction[] allTxs2 = sampleTxs.generateSimpleInvalidSig(50, 5, 5, 0.005);
      Transaction[] allTxs3 = sampleTxs.generateSimpleInvalidSig(100, 5, 5, 0.005);

      String common = "DropboxSampleTxsTest2-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }

   public static void writeDropboxTest3(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 3: test handleTransactions() with simple but "
            + "some invalid transactions because of inputSum < outputSum");

      Transaction[] allTxs1 = sampleTxs.generateSimpleInvalidValue(2, 5, 5, 0.005);
      Transaction[] allTxs2 = sampleTxs.generateSimpleInvalidValue(50, 5, 5, 0.005);
      Transaction[] allTxs3 = sampleTxs.generateSimpleInvalidValue(100, 5, 5, 0.005);

      String common = "DropboxSampleTxsTest3-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }

   public static void writeDropboxTest4(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 4: test handleTransactions() with simple and "
            + "valid transactions with some double spends");

      Transaction[] allTxs1 = sampleTxs.generateSimpleValidDoubleSpends(2, 5, 5, 0.005);
      Transaction[] allTxs2 = sampleTxs.generateSimpleValidDoubleSpends(50, 5, 5, 0.005);
      Transaction[] allTxs3 = sampleTxs.generateSimpleValidDoubleSpends(100, 5, 5, 0.005);

      String common = "DropboxSampleTxsTest4-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }

   public static void writeDropboxTest5(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 5: test handleTransactions() with valid but "
            + "some transactions are simple, some depend on other transactions");

      Transaction[] allTxs1 = sampleTxs.generateValidDependent(2, 5, 5, 0.005, 0.30);
      Transaction[] allTxs2 = sampleTxs.generateValidDependent(50, 5, 5, 0.005, 0.55);
      Transaction[] allTxs3 = sampleTxs.generateValidDependent(100, 5, 5, 0.005, 0.80);

      String common = "DropboxSampleTxsTest5-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }

   public static void writeDropboxTest6(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 6: test handleTransactions() with valid and simple but "
            + "some transactions take inputs from non-exisiting utxo's");

      Transaction[] allTxs1 = sampleTxs.generateNonExisiting(2, 5, 5, 0.005, 0.30);
      Transaction[] allTxs2 = sampleTxs.generateNonExisiting(50, 5, 5, 0.005, 0.55);
      Transaction[] allTxs3 = sampleTxs.generateNonExisiting(100, 5, 5, 0.005, 0.80);

      String common = "DropboxSampleTxsTest6-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }

   public static void writeDropboxTest7(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 7: test handleTransactions() with complex Transactions");

      Transaction[] allTxs1 = sampleTxs.generateComplex(2, 5, 5, 0.005, 0.30, 0.4);
      Transaction[] allTxs2 = sampleTxs.generateComplex(50, 5, 5, 0.005, 0.55, 0.3);
      Transaction[] allTxs3 = sampleTxs.generateComplex(100, 5, 5, 0.005, 0.80, 0.2);

      String common = "DropboxSampleTxsTest7-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }
   
   public static void writeDropboxTest8(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 8: test handleTransactions() with simple, valid transactions "
            + "being called again to check for changes made in the pool");

      Transaction[] allTxs1 = sampleTxs.generateSimpleValid(2, 5, 5, 0.005);
      Transaction[] allTxs2 = sampleTxs.generateSimpleValid(50, 5, 5, 0.005);
      Transaction[] allTxs3 = sampleTxs.generateSimpleValid(100, 5, 5, 0.005);
      
      String common = "DropboxSampleTxsTest8-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }
   
   public static void writeDropboxMaxFeesTest1(SampleTxs sampleTxs) 
         throws FileNotFoundException, IOException {
      System.out.println("Test 1: test handleTransactions() with complex transactions");

      Transaction[] allTxs1 = sampleTxs.generateComplex(2, 2, 2, 0.005, 1, 0.0);
      Transaction[] allTxs2 = sampleTxs.generateComplex(10, 3, 3, 0.005, 1, 0.2);
      Transaction[] allTxs3 = sampleTxs.generateComplex(30, 2, 2, 0.005, 0.50, 0.2);

      String common = "DropboxSampleMaxFeeTxsTest1-";
      String file1 = common + "1.txt";
      String file2 = common + "2.txt";
      String file3 = common + "3.txt";
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs1, file1);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs2, file2);
      TransactionsArrayFileHandler.writeTransactionsToFile(allTxs3, file3);
   }

   public static void main(String args[]) throws FileNotFoundException, IOException {
      System.out.println("Generating sample transactions for dropbox test");

      String skpFile = "DropboxSampleKeyPairs.txt";
      String supFile = "DropboxSampleUTXOPool.txt";
      SampleKeyPairs skp = SampleKeyPairsFileHandler.readKeyPairsFromFile(skpFile);
      SampleUTXOPool sup = SampleUTXOPoolFileHandler.readSampleUTXOPoolFromFile(skp, supFile);
      SampleTxs st = new SampleTxs(skp, sup);

      writeDropboxTest1(st);
      writeDropboxTest2(st);
      writeDropboxTest3(st);
      writeDropboxTest4(st);
      writeDropboxTest5(st);
      writeDropboxTest6(st);
      writeDropboxTest7(st);
      writeDropboxTest8(st);
      
      System.out.println("Generating sample transactions for dropbox test for maximum fees");

      String skpFileMaxFees = "DropboxSampleMaxFeeKeyPairs.txt";
      String supFileMaxFees = "DropboxSampleMaxFeeUTXOPool.txt";
      SampleKeyPairs skpMaxFees = SampleKeyPairsFileHandler.readKeyPairsFromFile(skpFileMaxFees);
      SampleUTXOPool supMaxFees = SampleUTXOPoolFileHandler.readSampleUTXOPoolFromFile(skpMaxFees, supFileMaxFees);
      SampleTxs stMaxFees = new SampleTxs(skpMaxFees, supMaxFees);

      writeDropboxMaxFeesTest1(stMaxFees);
   }
}
