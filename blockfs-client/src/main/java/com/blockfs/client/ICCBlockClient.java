package com.blockfs.client;


import java.security.PublicKey;
import java.util.List;

import com.blockfs.client.exception.*;


public interface ICCBlockClient {

    void FS_init()
            throws NoCardDetectedException, IBlockServerRequests.IntegrityException;

    void FS_write(int pos, int size, byte[] contents)
            throws UninitializedFSException, IBlockServerRequests.IntegrityException,
            ServerRespondedErrorException, ClientProblemException, WrongCardPINException;

    int FS_read(PublicKey key, int pos, int size, byte[] contents)
            throws IBlockServerRequests.IntegrityException, ServerRespondedErrorException;

    List<PublicKey> FS_list();
    class UninitializedFSException extends Exception {}
}