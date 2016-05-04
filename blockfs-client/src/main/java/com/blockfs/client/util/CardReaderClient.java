package com.blockfs.client.util;

import com.blockfs.client.exception.NoCardDetectedException;
import com.blockfs.client.exception.WrongCardPINException;
import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CardReaderClient {




    public static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException{
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateEncoded);
        X509Certificate cert = (X509Certificate)f.generateCertificate(in);
        return cert;
    }

    // Returns the n-th certificate, starting from 0
    public static  byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif

        } catch (PteidException e) {
            e.printStackTrace();
        }
        return certificate_bytes;
    }


    public static X509Certificate getCertificateFromCard() throws NoCardDetectedException {
        X509Certificate cert = null;
        System.loadLibrary("pteidlibj");


        try {
            pteid.Init(""); // Initializes the eID Lib
            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
            cert = getCertFromByteArray(getCertificateInBytes(0));
            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib
        } catch (CertificateException e) {

            e.printStackTrace();
        } catch (PteidException e) {
            throw new NoCardDetectedException();
        }

        return cert;
    }

    public static byte[] signWithCard(byte[] data) throws WrongCardPINException {

        byte[] signature = null;
        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String libName = "libbeidpkcs11.so";
        if (-1 != osName.indexOf("Windows"))
            libName = "pteidpkcs11.dll";
        else if (-1 != osName.indexOf("Mac"))
            libName = "pteidpkcs11.dylib";
        Class pkcs11Class = null;
        try {
            pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
            if (javaVersion.startsWith("1.5."))
            {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
                pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, null, false });
            }
            else
            {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
                pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
            }


            long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

            // Token login
            try {
                pkcs11.C_Login(p11_session, 1, null);
            }catch (PKCS11Exception e){
                if(e.getErrorCode() == PKCS11Constants.CKR_USER_ALREADY_LOGGED_IN){
                    System.out.println("Already logged In");
                }else{
                    throw e;
                }
            }
            CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);


            // Get available keys
            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
            attributes[0] = new CK_ATTRIBUTE();
            attributes[0].type = PKCS11Constants.CKA_CLASS;
            attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

            pkcs11.C_FindObjectsInit(p11_session, attributes);
            long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);

            // points to auth_key

            long signatureKey = keyHandles[0];		//test with other keys to see what you get
            pkcs11.C_FindObjectsFinal(p11_session);

            // initialize the signature method
            CK_MECHANISM mechanism = new CK_MECHANISM();
            mechanism.mechanism = PKCS11Constants.CKM_SHA256_RSA_PKCS;
            mechanism.pParameter = null;
            pkcs11.C_SignInit(p11_session, mechanism, signatureKey);

            // sign
            signature = pkcs11.C_Sign(p11_session, data);



            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib

        } catch (ClassNotFoundException| PteidException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            try {
                pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib
            } catch (PteidException ex) {
                ex.printStackTrace();
            }
        } catch (PKCS11Exception e) {
            if(e.getErrorCode() == PKCS11Constants.CKR_FUNCTION_CANCELED){
                throw new WrongCardPINException("No PIN inserted. Try again!");
            }
            else if(e.getErrorCode() == PKCS11Constants.CKR_PIN_INCORRECT){
                throw new WrongCardPINException("Wrong PIN inserted. Try again!");
            }
            else {
                e.printStackTrace();
                throw new WrongCardPINException("General error. Card Reader.");

            }
        }
        return signature;

    }



}
