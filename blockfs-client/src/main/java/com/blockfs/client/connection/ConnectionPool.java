package com.blockfs.client.connection;

import com.blockfs.client.exception.ServerRespondedErrorException;
import com.blockfs.client.rest.RestClient;
import com.blockfs.client.rest.model.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    public Block read(final String id) throws ServerRespondedErrorException{
        AtomicInteger count = new AtomicInteger();

        CompletionService<Block> completionService = new ExecutorCompletionService<Block>(executor);

        //TODO: Optimization for content blocks
        //TODO: Timestamp verification

        for(final String node : this.nodes) {
            completionService.submit(new Callable<Block>() {
                @Override
                public Block call() throws Exception {
                    Block block = RestClient.GET(id, node);
                    return block;
                }
            });
        }

        try {

            List<Block> received = new LinkedList<Block>();

            while(count.get() < QUORUMSIZE) {
                Future<Block> future = completionService.take();
                count.incrementAndGet();

                received.add(future.get());
            }

            return received.get(0);

        } catch (InterruptedException e) {
            throw new ServerRespondedErrorException();
        } catch (ExecutionException e) {
            throw new ServerRespondedErrorException();
        }

    }

    public String writePK(final byte[] data, final byte[] signature, final byte[] pubKey) throws ServerRespondedErrorException {
        AtomicInteger count = new AtomicInteger();

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

        try {

            List<String> received = new LinkedList<String>();

            while(count.get() < QUORUMSIZE) {
                Future<String> future = completionService.take();
                count.incrementAndGet();

                received.add(future.get());
            }

            return received.get(0);

        } catch (InterruptedException e) {
            throw new ServerRespondedErrorException();
        } catch (ExecutionException e) {
            throw new ServerRespondedErrorException();
        }

    }

    public String writeCBlock(final byte[] data) throws ServerRespondedErrorException {
        AtomicInteger count = new AtomicInteger();

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

        try {

            List<String> received = new LinkedList<String>();

            while(count.get() < 2) {
                Future<String> future = completionService.take();
                count.incrementAndGet();

                received.add(future.get());
            }

            return received.get(0);

        } catch (InterruptedException e) {
            throw new ServerRespondedErrorException();
        } catch (ExecutionException e) {
            throw new ServerRespondedErrorException();
        }

    }

}
