package com.blockfs.server;

public class BlockFS {

    public static void main(String[] args) {

        int port1 = 5050;
        if(args.length == 1){
            port1 = Integer.parseInt(args[0]);
        }

        new BlockFSController(port1);

    }
}

