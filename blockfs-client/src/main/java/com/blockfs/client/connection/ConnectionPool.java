package com.blockfs.client.connection;

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
        int count = 0;

        CompletionService<Block> completionService = new ExecutorCompletionService<Block>(executor);

        //TODO: Optimization for content blocks
        //TODO: Timestamp verification

        for(final String node : this.nodes) {
            completionService.submit(new Callable<Block>() {
                @Override
                public Block call() throws Exception {
                    Block block = RestClient.GET(id, node);

                    task.validation(block);

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

    public String writePK(final byte[] data, final byte[] signature, final byte[] pubKey, final PoolTask task) throws ServerRespondedErrorException {
        int count = 0;

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

            while(count < QUORUMSIZE) {
                try {
                    Future<String> future = completionService.take();
                    received.add(future.get());

                    count = count + 1;
                } catch (InterruptedException | ExecutionException e) {
                    continue;
                }
            }

            return received.get(0);


    }

    public String writeCBlock(final byte[] data) throws ServerRespondedErrorException {
        int count = 0;

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

            while(count < 2) {
                try {
                    Future<String> future = completionService.take();
                    received.add(future.get());

                    count = count + 1;
                } catch (InterruptedException | ExecutionException e) {
                    continue;
                }
            }

            return received.get(0);


    }

}
