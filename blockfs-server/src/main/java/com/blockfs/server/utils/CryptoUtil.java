package com.blockfs.server.utils;

import org.apache.commons.codec.binary.Base32;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtil {

    private static Base32 Base32 = new Base32();

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

}
