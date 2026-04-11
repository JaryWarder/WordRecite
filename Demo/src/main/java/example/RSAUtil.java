package example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public class RSAUtil {
    private static final String RSA_KEY_STORE = "RSAKey.txt";

    /**
     * 生成密钥对
     * 
     * @return KeyPair
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA",
                new org.bouncycastle.jce.provider.BouncyCastleProvider());
        final int KEY_SIZE = 1024;
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        System.out.println(keyPair.getPrivate());
        System.out.println(keyPair.getPublic());
        saveKeyPair(keyPair);
        return keyPair;
    }

    public static KeyPair getKeyPair() throws Exception {
        try (FileInputStream fis = new FileInputStream(RSA_KEY_STORE);
                ObjectInputStream oos = new ObjectInputStream(fis)) {
            return (KeyPair) oos.readObject();
        }
    }

    private static void saveKeyPair(KeyPair kp) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(RSA_KEY_STORE);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(kp);
        }
    }

    /**
     * 生成公钥
     *
     * @param modulus        模
     * @param publicExponent 指数
     * @return RSAPublicKey
     */
    public static RSAPublicKey generateRSAPublicKey(byte[] modulus,
            byte[] publicExponent) throws Exception {
        KeyFactory keyFac;
        try {
            keyFac = KeyFactory.getInstance("RSA",
                    new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new Exception(ex.getMessage());
        }

        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(
                modulus), new BigInteger(publicExponent));
        try {
            return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * 生成私钥
     *
     * @param modulus *
     * @param privateExponent *
     * @return RSAPrivateKey
     */
    public static RSAPrivateKey generateRSAPrivateKey(byte[] modulus,
            byte[] privateExponent) throws Exception {
        KeyFactory keyFac;
        try {
            keyFac = KeyFactory.getInstance("RSA",
                    new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new Exception(ex.getMessage());
        }

        RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(new BigInteger(
                modulus), new BigInteger(privateExponent));
        try {
            return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
        } catch (InvalidKeySpecException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * 加密
     *
     * @param pk   加密的密钥
     * @param data 待加密的明文数据
     * @return 加密后的数据
     */
    public static byte[] encrypt(PublicKey pk, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA",
                new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        int blockSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(data.length);
        int leavedSize = data.length % blockSize;
        int blocksSize = leavedSize != 0 ? data.length / blockSize + 1
                : data.length / blockSize;
        byte[] raw = new byte[outputSize * blocksSize];
        int i = 0;
        while (data.length - i * blockSize > 0) {
            if (data.length - i * blockSize > blockSize) {
                cipher.doFinal(data, i * blockSize, blockSize, raw, i * outputSize);
            } else {
                cipher.doFinal(data, i * blockSize, data.length - i * blockSize, raw, i * outputSize);
            }
            i++;
        }
        return raw;
    }

    /**
     * 解密
     * 
     * @param pk  解密的密钥
     * @param raw 已经加密的数据
     * @return 解密后的明文
     */
    public static byte[] decrypt(PrivateKey pk, byte[] raw) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding",
                new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, pk);
        return cipher.doFinal(raw);
    }

    public static byte[] decryptNoPadding(PrivateKey pk, byte[] raw) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding",
                new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, pk);
        return cipher.doFinal(raw);
    }

}
