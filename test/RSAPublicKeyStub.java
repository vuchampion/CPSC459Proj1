import java.math.BigInteger;


public class RSAPublicKeyStub {
   private BigInteger exponent;
   private BigInteger modulus;
   public RSAPublicKeyStub(BigInteger e, BigInteger m) {
      exponent = e;
      modulus = m;
   }
   public BigInteger getExponent() {
      return exponent;
   }

   public BigInteger getModulus() {
      return modulus;
   }

   public int hashCode() {
      int hashCode = exponent.hashCode();
      hashCode = (int)(31*hashCode + modulus.hashCode());
      return hashCode;
   }

   public boolean equals(Object other) {
      if (other == null) {
         return false;
      }
      if (getClass() != other.getClass()) {
         return false;
      }
      
      RSAPublicKeyStub pub = (RSAPublicKeyStub) other;
      BigInteger exp = pub.getExponent();
      BigInteger mod = pub.getModulus();
      if (exp.equals(exponent) && mod.equals(modulus))
         return true;
      return false;
   }

}
