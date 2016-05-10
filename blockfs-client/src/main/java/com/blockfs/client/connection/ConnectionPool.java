package com.blockfs.client.connection;

import com.blockfs.client.CCBlockClient;
import com.blockfs.client.exception.NoQuorumException;
import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.model.PKBlock;
import com.blockfs.client.IBlockServerRequests;
import com.blockfs.client.rest.RestClient;
import com.blockfs.client.rest.model.Block;
import com.blockfs.client.util.CryptoUtil;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ConnectionPool {
    private int QUORUMSIZE;
    private List<String> nodes;
    private ExecutorService executor;
    private final int f = 1; //allowed byzantine
    private final int readCBQuorumSize = 1;
    private final int writeCBQuorumSize = f + 1;
    public int version = 0;

    public ConnectionPool(List<String> nodes) {
        this.nodes = nodes;
        this.executor = Executors.newFixedThreadPool(nodes.size());
        this.QUORUMSIZE = (nodes.size() + f)/2;
    }

    public ConnectionPool() {
        this.nodes = new LinkedList<String>();
    }

    public void addNode(String node) {
        this.nodes.add(node);
    }


    public Block readPK(final String id, PoolTask task) throws ServerRespondedErrorException {
        PKBlock fresh = null;
        for (Block block : read(id, QUORUMSIZE, task)) {

            PKBlock pk = (PKBlock) block;
            if (fresh == null || pk.getTimestamp() >= CCBlockClient.sequence)
                fresh = pk;
        }
        return fresh;
    }

    public Block readCB(final String id, PoolTask task) throws ServerRespondedErrorException {
        return read(id, this.readCBQuorumSize, task).get(0);
    }

    public List<Block> read(final String id, final int quorumSize, final PoolTask task) throws ServerRespondedErrorException{

        CompletionService<Block> completionService = new ExecutorCompletionService<Block>(executor);


        for(final String node : this.nodes) {
            completionService.submit(new Callable<Block>() {
                @Override
                public Block call() throws Exception {
                    Block block = RestClient.GET(id, node);

                    task.validation(id, block);

                    return block;
                }
            });
        }

        List<Block> received = new LinkedList<Block>();
        int success = 0, failure=0;
        while(success < quorumSize) {

            if (success + failure >= this.nodes.size())
                throw new NoQuorumException(String.format("%d in %d nodes failed.", failure, nodes.size()));

            try {
                //TODO verificar se .take() reage a timeout
                Future<Block> future = completionService.take();
                received.add(future.get());

                success += 1;
            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
                if(e.getCause() != null && e.getCause().getMessage() != null &&
                        e.getCause().getMessage().startsWith("404")){
                    //if not found in server
                    success += 1;
                }else {
                    failure += 1;
                }
            }

        }
        if(received.size() == 0){
            throw new ServerRespondedErrorException("404");
        }else
            return received;


    }

    public String writePK(final byte[] data, final byte[] signature, final byte[] pubKey) throws IBlockServerRequests.IntegrityException {
        int count = 0;
        int fails = 0;

        CompletionService<String> completionService = new ExecutorCompletionService<String>(executor);

        for(final String node : this.nodes) {
            completionService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String result = RestClient.POST_pkblock(data, signature, pubKey, node);

                    return result;
                }
            });
        }

            List<String> received = new LinkedList<String>();
            while((count) < QUORUMSIZE && ((fails + count) < nodes.size())) {
                try {
                    Future<String> future = completionService.take();
                    String pkHash = future.get();

                    if (CryptoUtil.generateHash(pubKey).equals(pkHash)){
                        received.add(pkHash);
                        count = count + 1;
                    } else {
                        fails = fails + 1;
                    }
                } catch (InterruptedException | ExecutionException  e) {
                    fails = fails + 1;
                    e.printStackTrace();
                    continue;
                }
            }

        if(received.size() >= QUORUMSIZE)
            return received.get(0);
        else
            throw new IBlockServerRequests.IntegrityException("PUT_H: invalid data hash received");

    }

    public String writeCBlock(final byte[] data) throws IBlockServerRequests.IntegrityException {
        int count = 0;
        int fails = 0;

        CompletionService<String> completionService = new ExecutorCompletionService<String>(executor);

        for(final String node : this.nodes) {
            completionService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String result = RestClient.POST_cblock(data, node);
                    return result;
                }
            });
        }


        List<String> received = new LinkedList<String>();

            while(count < 2 && ((fails + count) < nodes.size())) {
                try {
                    Future<String> future = completionService.take();


                    String dataHash = future.get();

                    if (CryptoUtil.generateHash(data).equals(dataHash)){
                        received.add(dataHash);
                        count = count + 1;
                    } else {
                        fails = fails + 1;
                    }

                } catch (InterruptedException | ExecutionException e) {
                    fails = fails + 1;
                    continue;
                }
            }

        if(received.size() > 0)
            return received.get(0);
        else
            throw new IBlockServerRequests.IntegrityException("PUT_H: invalid data hash received");

    }

    public void storePubKey(X509Certificate certificate) throws IBlockServerRequests.IntegrityException {
        int count = 0;
        int fails = 0;

        CompletionService<String> completionService = new ExecutorCompletionService<String>(executor);

        for(final String node : this.nodes) {
            completionService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    RestClient.POST_certificate(certificate, version, node);
                    System.out.println(node  + " : POST_certificate");
                    return node;
                }
            });
        }

        while((count) < QUORUMSIZE && ((fails + count) < nodes.size())) {

            try {
                Future<String> future = completionService.take();
                future.get();


                count = count + 1;
            } catch (InterruptedException | ExecutionException e) {
                fails = fails + 1;

            }

        }
        if(count >= QUORUMSIZE) {
            return;
        }
        else
            throw new IBlockServerRequests.IntegrityException("PUT_H: invalid data hash received");

    }


}
