
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Misuse {

    /**
     * Misuse: "AES" is not secure, "AES/GCM/PKCS5Padding" should be used to get the cipher
     */
    public void test() {
        try {
            String plainText = "Sensitive information";
            int keySize = 128;
            // Generate a key for AES
            KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
            keygenerator.init(keySize);
            SecretKey key = keygenerator.generateKey();
            // Encrypt the plain text with AES
            Cipher aesChipher;
            aesChipher = Cipher.getInstance("AES");
            aesChipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted= aesChipher.doFinal(plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }


    public void test1() {
        File file = new File();
        file.open();
        file.close();
    }
}
