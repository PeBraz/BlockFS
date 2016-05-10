package com.blockfs.client.connection;

import com.blockfs.client.IBlockServerRequests;
import com.blockfs.client.exception.ServerRespondedErrorException;
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
    public int version = 0;

    public ConnectionPool(List<String> nodes) {
        this.nodes = nodes;
        this.executor = Executors.newFixedThreadPool(nodes.size());
        this.QUORUMSIZE = (nodes.size()/2) + 1;
    }

    public ConnectionPool() {
        this.nodes = new LinkedList<String>();
    }

    public void addNode(String node) {
        this.nodes.add(node);
    }

    public Block read(final String id, final PoolTask task) throws ServerRespondedErrorException{
        int count = 0;

        CompletionService<Block> completionService = new ExecutorCompletionService<Block>(executor);

        //TODO: Optimization for content blocks
        //TODO: Timestamp verification

        for(final String node : this.nodes) {
            completionService.submit(new Callable<Block>() {
                @Override
                public Block call() throws Exception {
                    Block block = RestClient.GET(id, node);

                    task.validation(id, block);

                    return block;
                };
            });
        }


            List<Block> received = new LinkedList<Block>();

            //TODO: Stop if no more tasks in CompletionService
            while(count < QUORUMSIZE) {
                try {
                    Future<Block> future = completionService.take();
                    received.add(future.get());

                    count = count + 1;
                } catch (InterruptedException | ExecutionException e) {
                    continue;
                }
            }

            return received.get(0);


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
                System.out.println(" : take");
                Future<String> future = completionService.take();
                System.out.println(future.get()  + " : received");


                count = count + 1;
            } catch (InterruptedException | ExecutionException e) {
                fails = fails + 1;

            }
            System.out.println("count: "  + count);
            System.out.println("fails: "  + fails);
            System.out.println("(count) < QUORUMSIZE : " + ((count) < QUORUMSIZE));
        }
        System.out.println(" : exit");
        if(count >= QUORUMSIZE) {
            System.out.println(" : return");
            return;
        }
        else
            throw new IBlockServerRequests.IntegrityException("PUT_H: invalid data hash received");



    }

}
