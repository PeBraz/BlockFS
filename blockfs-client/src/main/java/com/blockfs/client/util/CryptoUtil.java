package com.blockfs.client.util;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtil {

    private static Base32 Base32 = new Base32();
    private static String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public static String generateHash(byte[] publicKey) {
        byte[] hash = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(publicKey);
            hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return Base32.encodeAsString(hash);
    }

    public static boolean verifySignature(byte[] data, byte[] signature, byte[] publicKey) {
        boolean isCorrect = false;

        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
            sig.initVerify(pk);
            sig.update(data);
            isCorrect = sig.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return isCorrect;

    }

    public static String calculateHMAC(String data, String secret) throws SignatureException {

        String result;

        Digest digest = new SHA256Digest();
        KeyParameter key = new KeyParameter(secret.getBytes());

        HMac mac = new HMac(digest);
        mac.init(key);
        mac.update(data.getBytes(), 0, data.getBytes().length);

        byte[] rawMac = new byte[digest.getDigestSize()];
        mac.doFinal(rawMac, 0);

        result = Base64.encodeBase64String(rawMac);

        return result;


    }

    public static boolean verifyHMAC(String data, String secret, String hmac) throws SignatureException {

        return calculateHMAC(data, secret).equals(hmac);
    }

}
