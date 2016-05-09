package com.blockfs.server.utils;

import org.apache.commons.codec.binary.Base32;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Random;

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

    public static X509Certificate generateCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, InvalidKeyException, SignatureException, CertIOException, OperatorCreationException {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Date dateOfIssuing = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date dateOfExpiry = new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000);

        // signers name
        X500Name issuerName = new X500Name("CN=G15");

        // subjects name - the same as we are self signed.
        X500Name subjectName = issuerName;

        // serial
        BigInteger serial = BigInteger.valueOf(new Random().nextInt());

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        X509v3CertificateBuilder v3CertGen = new X509v3CertificateBuilder(issuerName, serial, dateOfIssuing, dateOfExpiry, subjectName, SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));

        v3CertGen.addExtension(
                X509Extension.subjectKeyIdentifier,
                false,
                extUtils.createSubjectKeyIdentifier(keyPair.getPublic()));

        v3CertGen.addExtension(
                X509Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier(keyPair.getPublic()));

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(v3CertGen.build(new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(keyPair.getPrivate())));
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

    public static boolean verifyHMAC(String data, String secret, String hmac) throws SignatureException {

        return calculateHMAC(data, secret).equals(hmac);
    }

}
