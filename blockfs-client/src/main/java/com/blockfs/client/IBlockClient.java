package com.blockfs.client;


import java.io.IOException;

public interface IBlockClient {

    String FS_init();
    void FS_write(int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, UninitializedFSException;
    int FS_read(String hash, int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException;

     class UninitializedFSException extends Exception {}
 }
