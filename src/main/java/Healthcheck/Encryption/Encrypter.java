package Healthcheck.Encryption;

import Healthcheck.AppProperties;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Encrypter
{
   private Cipher _ecipher;
   private Cipher _decipher;

   private static final Encrypter _encrypter;

    static
    {
        Encrypter encrypter = null;
        try
        {
            String key = AppProperties.GetInstance().Properties.getProperty("SecretKey");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
            encrypter = new Encrypter(secretKey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            _encrypter = encrypter;
        }
    }

   private Encrypter(SecretKey key) throws EncrypterException
   {
       try
       {
           _ecipher = Cipher.getInstance("AES");
           _decipher = Cipher.getInstance("AES");

           _ecipher.init(Cipher.ENCRYPT_MODE, key);
           _decipher.init(Cipher.DECRYPT_MODE, key);
       }
       catch (NoSuchAlgorithmException|NoSuchPaddingException|InvalidKeyException e)
       {
           throw new EncrypterException("Initialization of Healthcheck.Encryption.Encrypter failed");
       }
   }

   public static Encrypter GetInstance()
   {
       return _encrypter;
   }

   public String Encrypt(String str) throws EncrypterException
   {
       try
       {
           byte[] utf8 = str.getBytes("UTF8");
           byte[] enc = _ecipher.doFinal(utf8);
           return Base64.getEncoder().encodeToString(enc);
       }
       catch (BadPaddingException|UnsupportedEncodingException|IllegalBlockSizeException e)
       {
           throw new EncrypterException("Encryption failed.");
       }
   }

   public String Decrypt(String str) throws EncrypterException
   {
       try
       {
           byte[] dec = Base64.getDecoder().decode(str);
           byte[] utf8 = _decipher.doFinal(dec);
           return new String(utf8, "UTF8");
       }
       catch (BadPaddingException|UnsupportedEncodingException|IllegalBlockSizeException|IllegalArgumentException e)
       {
           throw new EncrypterException("Decryption failed.");
       }
   }
}
