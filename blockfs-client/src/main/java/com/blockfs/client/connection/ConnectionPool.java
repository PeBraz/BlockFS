package com.blockfs.client.connection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPool {
    private final int QUORUMSIZE = 5;
    private List<String> nodes;
    private ExecutorService executor;

    public ConnectionPool(List<String> nodes) {
        this.nodes = nodes;
        this.executor = Executors.newFixedThreadPool(nodes.size());
    }

    public ConnectionPool() {
        this.nodes = new LinkedList<String>();
    }

    public void addNode(String node) {
        this.nodes.add(node);
    }

    public void read(String id) {
        AtomicInteger count = new AtomicInteger();

        CompletionService<String> completionService = new ExecutorCompletionService<String>(executor);

        for(final String node : this.nodes) {
            completionService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "Node URL: " + node;
                }
            });
        }

        try {

        while(count.get() < QUORUMSIZE) {
            Future<String> future = completionService.take();
            count.incrementAndGet();

            System.out.println(future.get());
        }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

}
