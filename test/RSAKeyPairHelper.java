
public class RSAKeyPairHelper {
   private RSAKey pubKey;
   private RSAKey privKey;

   public RSAKeyPairHelper(RSAKey pubKey, RSAKey privKey) {
      this.pubKey = pubKey;
      this.privKey = privKey;
   }
   
   public RSAKey getPrivateKey() {
      return privKey;
   }
   
   public RSAKey getPublicKey() {
      return pubKey;
   }
}
