package com.blockfs.client;


import java.io.IOException;

public interface IBlockClient {

    void PS_init();
    void FS_write(int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException;
    int FS_read(String hash, int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException;
 }
