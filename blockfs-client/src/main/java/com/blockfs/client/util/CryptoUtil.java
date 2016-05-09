package com.blockfs.client.util;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
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

        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), HMAC_SHA256_ALGORITHM);

            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(key);

            byte[] rawMac = mac.doFinal(data.getBytes());

            result = Base64.encodeBase64String(rawMac);

            return result;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SignatureException("Failed to generate HMAC: " + e.getMessage());
        }

    }

}
