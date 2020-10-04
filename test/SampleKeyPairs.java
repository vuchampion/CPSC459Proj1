import java.util.ArrayList;
import java.util.HashMap;

public class SampleKeyPairs {
   private ArrayList<RSAKeyPairHelper> people;
   private HashMap<RSAPublicKeyStub, Integer> pubKeyMapping;

   public SampleKeyPairs(int k) {
      generate(k);
   }
   
   public SampleKeyPairs(ArrayList<RSAKeyPairHelper> p, HashMap<RSAPublicKeyStub, Integer> h) {
      people = p;
      pubKeyMapping = h;
   }

   // generate k RSA key pairs
   private void generate(int k) {
      people = new ArrayList<RSAKeyPairHelper>();
      pubKeyMapping = new HashMap<RSAPublicKeyStub, Integer>();
      byte[] initialKey = new byte[PRGen.KeySizeBytes];
      for (int i = 0; i < initialKey.length; i++)
         initialKey[i] = (byte) i;
      PRGen prg = new PRGen(initialKey);
      int numSizeBits = 2048;
      for (int i = 0; i < k; i++) {
         byte[] key = new byte[PRGen.KeySizeBytes];
         for (int j = 0; j < key.length; j++) {
            key[i] = (byte) prg.next(8);
         }
         PRGen p = new PRGen(key);
         System.out.println("Generating key pair " + i + " of " + k);
         RSAKeyPair rp = new RSAKeyPair(p, numSizeBits);
         people.add(new RSAKeyPairHelper(rp.getPublicKey(), rp.getPrivateKey()));
         pubKeyMapping.put(new RSAPublicKeyStub(rp.getPublicKey().getExponent(), 
               rp.getPublicKey().getModulus()), i);
      }
   }

   public ArrayList<RSAKeyPairHelper> getPeople() {
      return people;
   }

   public int getPerson(RSAKey addr) {
      RSAPublicKeyStub pub = new RSAPublicKeyStub(addr.getExponent(), addr.getModulus());
      Integer index = pubKeyMapping.get(pub);
      if (index == null)
         return -1;
      else 
         return index; 
   }
}
