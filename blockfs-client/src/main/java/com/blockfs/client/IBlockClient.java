package com.blockfs.client;


public interface IBlockClient {

    void PS_init();
    void FS_write(int pos, int size, byte[] contents);
    int FS_read(String hash, int pos, int size, byte[] contents);
 }
