import java.io.FileNotFoundException;
import java.io.IOException;


public class GenerateAllTxtFiles {
   public static void main(String args[]) throws FileNotFoundException, IOException {
      SampleKeyPairsFileHandler.main(args);
      SampleUTXOPoolFileHandler.main(args);
      SampleTxsFileHandler.main(args);
   }
}
