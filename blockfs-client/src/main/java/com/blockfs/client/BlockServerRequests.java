package com.blockfs.client;


import com.blockfs.client.rest.RestClient;
import com.blockfs.client.rest.model.Block;
import com.blockfs.client.rest.model.PKBlock;

public class BlockServerRequests implements IBlockServerRequests{


    public Block get(String id) throws ServerRespondedErrorException, IntegrityException {
        //TODO Block discrimination + integrity (verify signature)


        Block result = RestClient.GET(id);
        if(result.getType() == Block.PUBLIC){
            PKBlock pub = (PKBlock)result;
            String hash = "PK"+CryptoUtil.generateHash(pub.getPublicKey());
            if(!hash.equals(id))
                throw new IntegrityException("PubKey hash is different from returned pub key");
            if(!CryptoUtil.verifySignature(pub.getData(), pub.getSignature(), pub.getPublicKey())){
                throw new IntegrityException("Os dados retornados foram modificados");
            }
        }

        return result;
    }

    public String put_k(byte[] data, byte[] signature, byte[] pubKey) throws IntegrityException {

        String pkHash = RestClient.POST_pkblock(data, signature, pubKey);

        if (!CryptoUtil.generateHash(pubKey).equals(pkHash))
            throw new IntegrityException("PUT_K: invalid public key hash received");

        return pkHash;
    }
    public String put_h(byte[] data) throws IntegrityException{

        String dataHash = RestClient.POST_cblock(data);

        if (!CryptoUtil.generateHash(data).equals(dataHash))
            throw new IntegrityException("PUT_H: invalid data hash received");

        return dataHash;
    }


}
