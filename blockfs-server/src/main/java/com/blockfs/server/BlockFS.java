package com.blockfs.server;

public class BlockFS {

    public static void main(String[] args) {

        int port1 = 5050;
        String option;
        if(args.length == 1){
            port1 = Integer.parseInt(args[0]);
        }

        if(args.length == 2){
            port1 = Integer.parseInt(args[0]);
            option = args[1];
            new ServerThird(port1, option);
        }

        if(args.length == 1 || args.length == 0)
            new BlockFSController(port1);

    }
}

