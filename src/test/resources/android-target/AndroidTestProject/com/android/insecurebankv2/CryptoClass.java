package com.android.insecurebankv2;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
The page that holds the logic for encryption and decryption used in the application
@author Dinesh Shetty
*/
public class CryptoClass {

    //	The super secret key used by the encryption function
    String key = "This is the super secret key 123";

    //	The initialization vector used by the encryption function
    byte[] ivBytes = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    String plainText;
    byte[] cipherData;
    String base64Text;
    String cipherText;


    /*
    The function that handles the aes256 encryption.
    ivBytes: Initialization vector used by the encryption function
    keyBytes: Key used as input by the encryption function
    textBytes: Plaintext input to the encryption function
    */
    public static byte[] aes256encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    /*
    The function that handles the aes256 encryption.
    ivBytes: Initialization vector used by the encryption function
    keyBytes: Key used as input by the encryption function
    textBytes: Plaintext input to the encryption function
    */
    public static byte[] aes256encryptVariant(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        //Linghui: the following misuse will cause the app crash: System.err java.security.InvalidKeyException: Unsupported key size: 64 bytes at com.android.org.conscrypt.OpenSSLCipher$EVP_CIPHER$AES.checkSupportedKeySize(OpenSSLCipher.java:733)
        keygen.init(46);// CogniCrypt reports "First parameter (with value 46) should be any of {128, 192, 256}"
        SecretKey key = keygen.generateKey();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);// CogniCrypt reports "Second parameter was not properly generatedKey"
        return cipher.doFinal(textBytes);
    }


    /*
	The function that handles the aes256 decryption.
	ivBytes: Initialization vector used by the decryption function
	keyBytes: Key used as input by the decryption function
	textBytes: Ciphertext input to the decryption function
	*/
    public static byte[] aes256decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);

    }

    /*
    The function that handles the aes256 decryption.
    ivBytes: Initialization vector used by the decryption function
    keyBytes: Key used as input by the decryption function
    textBytes: Ciphertext input to the decryption function
    */
    public static byte[] aes256decryptVariant(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);// CogniCrypt reports "First parameter was not properly randomized"
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// CogniCrypt reports "First parameter (with value "AES/ECB/PKCS5Padding") should be any of AES/{CBC, GCM, PCBC, CTR, CTS, CFB, OFB}"
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec); // CogniCrypt reports "Third parameter was not properly prepared"
        return cipher.doFinal(textBytes);
    }

    /*
    The function that uses the aes256 decryption function
    theString: Ciphertext input to the decryption function
    plainText: Plaintext output of the encryption operation
    */
    public String aesDeccryptedString(String theString) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = key.getBytes("UTF-8");
        //cipherData = CryptoClass.aes256decrypt(ivBytes, keyBytes, Base64.decode(theString.getBytes("UTF-8"), Base64.DEFAULT));
        //Linghui: the next line for missuse injection
        cipherData = CryptoClass.aes256decryptVariant(ivBytes, keyBytes, Base64.decode(theString.getBytes("UTF-8"), Base64.DEFAULT));
        plainText = new String(cipherData, "UTF-8");
        return plainText;
    }

    /*
    The function that uses the aes256 encryption function
    theString: Plaintext input to the encryption function
    cipherText: Ciphertext output of the encryption operation
    */
    public String aesEncryptedString(String theString) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = key.getBytes("UTF-8");
        plainText = theString;
        //cipherData = CryptoClass.aes256encrypt(ivBytes, keyBytes, plainText.getBytes("UTF-8"));
        //Linghui: the next line for missuse injection
        cipherData = CryptoClass.aes256encryptVariant(ivBytes, keyBytes, plainText.getBytes("UTF-8"));
        cipherText = Base64.encodeToString(cipherData, Base64.DEFAULT);
        return cipherText;
    }
}