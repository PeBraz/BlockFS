package com.blockfs.client.connection;

import com.blockfs.client.exception.NoQuorumException;
import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.RestClient;
import com.blockfs.client.rest.model.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ConnectionPool {
    private int QUORUMSIZE;
    private List<String> nodes;
    private ExecutorService executor;

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
        int success = 0, failure=0;
        while(success < QUORUMSIZE) {
            try {
                //TODO verificar se .take() reage a timeout
                Future<Block> future = completionService.take();
                received.add(future.get());

                success += 1;
            } catch (InterruptedException | ExecutionException e) {
                failure +=1;
            }
            if (success + failure >= this.nodes.size())
                throw new NoQuorumException(String.format("%d in %d nodes failed.", failure, nodes.size()));
        }

        return received.get(0);


    }

    public String writePK(final byte[] data, final byte[] signature, final byte[] pubKey, final PoolTask task) throws ServerRespondedErrorException {

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
        int success = 0, failure = 0;
        while(success < QUORUMSIZE) {
            try {
                Future<String> future = completionService.take();
                received.add(future.get());

                success += 1;
            } catch (InterruptedException | ExecutionException e) {
                failure += 1;
            }
            if (success + failure >= this.nodes.size())
                throw new NoQuorumException(String.format("%d in %d nodes failed.", failure, nodes.size()));
        }

        return received.get(0);
    }

    public String writeCBlock(final byte[] data) throws ServerRespondedErrorException {

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

        int success = 0, failure = 0;
        while(success < 2) {
            try {
                Future<String> future = completionService.take();
                received.add(future.get());

                success += 1;
            } catch (InterruptedException | ExecutionException e) {
                    continue;
            }
            if (success + failure >= this.nodes.size())
                throw new NoQuorumException(String.format("%d in %d nodes failed.", failure, nodes.size()));
        }

        return received.get(0);
    }

}
