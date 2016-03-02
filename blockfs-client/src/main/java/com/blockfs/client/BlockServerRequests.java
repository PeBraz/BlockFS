package com.blockfs.client;


import com.blockfs.server.utils.CryptoUtil;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class BlockServerRequests implements IBlockServerRequests{

    public byte[] get(String id){
        return new byte[0];
    }

    public String put_k(byte[] data, String signature, String pubKey) throws IntegrityException {
        //TODO Integrity Check (hash(pubkey) == id)
        return "";
    }
    public String put_h(byte[] data) throws IntegrityException{
        //TODO Integrity Check (hash(data) == id)
        return "";
    }


}
