package Security;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class DHCoder_EKE {
    private static final String KEY_ALGORITHM = "DH";
    /**
     * Symmetric key algorithm: AES
     */
    private static final String SELECT_ALGORITHM = "AES";
    //-Djdk.crypto.KeyAgreement.legacyKDF=true

    /**
     * Encryption
     * @param data data waiting encryption
     * @param secretKey secret key
     * @return byte[] encrypted data
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, Key secretKey) throws Exception{
        //encrypt data
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * decryption
     * @param data data waiting to be decrypted
     * @param secretKey secret key
     * @return byte[] decrypt data
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, Key secretKey) throws Exception{
        //get data
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * get safe random number
     * @return BigInteger with safe random number
     */
    public static BigInteger getA(){
        Random randomGenerator = new Random();
        BigInteger a = new BigInteger(1024, randomGenerator);
        return a;
    }

    /**
     * get p and g
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidParameterSpecException
     */
    public static BigInteger[] getP_G() throws NoSuchAlgorithmException, InvalidParameterSpecException {
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance(KEY_ALGORITHM);
        paramGen.init(1024, new SecureRandom());
        AlgorithmParameters params = paramGen.generateParameters();
        DHParameterSpec dhSpec = (DHParameterSpec)params.getParameterSpec(DHParameterSpec.class);
        BigInteger[] arr = new BigInteger[2];
        arr[0]  = dhSpec.getP();
        arr[1] = dhSpec.getG();
        return arr;
    }

    /**
     * generate key from the input byte[]
     * @param sharedKey
     * @return
     */
    public static Key generateKey(byte[] sharedKey)
    {
        // AES supports 128 bit keys. So, just take first 16 bits of DH generated key.
        byte[] byteKey = new byte[16];
        for(int i = 0; i < 16; i++) {
            byteKey[i] = sharedKey[i];
        }

        // convert given key to AES format
        try {
            Key key = new SecretKeySpec(byteKey, "AES");
            return key;
        } catch(Exception e) {
            System.err.println("Error while generating key: " + e);
        }
        return null;
    }
}