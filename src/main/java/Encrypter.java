import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.Base64;

public class Encrypter
{
   private Cipher _ecipher;
   private Cipher _decipher;

   public Encrypter(SecretKey key) throws Exception
   {
       _ecipher = Cipher.getInstance("AES");
       _decipher = Cipher.getInstance("AES");

       _ecipher.init(Cipher.ENCRYPT_MODE, key);
       _decipher.init(Cipher.DECRYPT_MODE, key);
   }

   public String encrypt(String str) throws Exception
   {
       byte[] utf8 = str.getBytes("UTF8");
       byte[] enc = _ecipher.doFinal(utf8);
       return Base64.getEncoder().encodeToString(enc);
   }

   public String decrypt(String str) throws Exception
   {
       byte[] dec = Base64.getDecoder().decode(str);
       byte[] utf8 = _decipher.doFinal(dec);
       return new String(utf8, "UTF8");
   }
}
