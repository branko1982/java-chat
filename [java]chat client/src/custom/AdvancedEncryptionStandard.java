package custom;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AdvancedEncryptionStandard {


    private AdvancedEncryptionStandard(){};
    private static AdvancedEncryptionStandard instance;

    public static AdvancedEncryptionStandard getInstance() {
        if (instance == null) {
            instance = new AdvancedEncryptionStandard();
        }
        return instance;
    }
    private byte[] key;

    private static final String ALGORITHM = "AES";

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] encrypt(byte[] plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plainText);

    }

    public byte[] decrypt(byte[] cipherText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(cipherText);
    }
    public byte[] getKey(){
        return key;
    }
}