package com.blockfs.server;


import com.blockfs.server.exceptions.InvalidCertificate;
import com.blockfs.server.exceptions.ReplayAttackException;
import com.blockfs.server.exceptions.WrongDataSignature;

import java.io.FileNotFoundException;
import java.security.cert.X509Certificate;
import java.util.List;

public interface IBlockServer {

    public byte[] get(String id) throws FileNotFoundException, WrongDataSignature;

    public String put_k(byte[] data, byte[] signature, byte[] publicKey) throws WrongDataSignature, ReplayAttackException;

    public String put_h(byte[] data);

    public void storePubKey(X509Certificate certificate, boolean verify) throws InvalidCertificate;

    public List<X509Certificate> readPubKeys();
}
