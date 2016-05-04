package com.blockfs.client.old;

import com.blockfs.client.IBlockServerRequests;
import com.blockfs.client.exception.ClientProblemException;
import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.exception.WrongPasswordException;

public interface IBlockClient {

    String FS_init(String name, String password) throws WrongPasswordException, ClientProblemException;
    void FS_write(int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, UninitializedFSException, ServerRespondedErrorException, ClientProblemException;
    int FS_read(String hash, int pos, int size, byte[] contents) throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException;

     class UninitializedFSException extends Exception {}
 }
