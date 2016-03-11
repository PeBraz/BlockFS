package com.blockfs.client;


public interface IBlockClient {

    String FS_init(String name, String password) throws WrongPasswordException, ClientProblemException;
    void FS_write(int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, UninitializedFSException, ServerRespondedErrorException, ClientProblemException;
    int FS_read(String hash, int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException;

     class UninitializedFSException extends Exception {}
 }
