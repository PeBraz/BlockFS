package com.blockfs.example;

import com.beust.jcommander.JCommander;
import com.blockfs.client.BlockClient;
import com.blockfs.client.IBlockClient;
import com.blockfs.client.IBlockServerRequests;
import com.blockfs.example.commands.GetCommand;
import com.blockfs.example.commands.InitCommand;
import com.blockfs.example.commands.PutCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class App
{

    public static void main( String[] args )
    {

        BlockClient blockClient = new BlockClient();

        JCommander jc = new JCommander();
        GetCommand get = new GetCommand();
        PutCommand put = new PutCommand();
        InitCommand init = new InitCommand();

        jc.addCommand("get", get);
        jc.addCommand("put", put);
        jc.addCommand("init", init);

        jc.parse(args);
        switch(jc.getParsedCommand()) {
            case "get":
                String output = new String(get(blockClient, get.hash.get(0), get.start, get.size));
                System.out.println(output);
                break;
            case "put":
                System.out.println(putFile(blockClient, put.filename.get(0)));
                break;
            case "init":
                init(blockClient);
                break;
            default:
                jc.usage();

        }

    }

    public static byte[] get(BlockClient bc, String hash, int start, int size) {

        byte[] read = new byte[size];

        try {
            bc.FS_read(hash, start, size, read);
        } catch (IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
        }

        return read;

    }

    public static String putFile(BlockClient bc, String filename) {

        String hash = null;

        try {
            File file = new File(filename);
            FileInputStream is = new FileInputStream(file);

            byte[] chunk = new byte[1024];
            int chunkLen = 0;
            int totalSize = 0;

            hash = bc.FS_init();

            while ((chunkLen = is.read(chunk)) != -1) {
                bc.FS_write(totalSize, chunkLen, chunk);
                totalSize += chunkLen;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IBlockClient.UninitializedFSException e) {
            e.printStackTrace();
        } catch (IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
        }

        return hash;
    }

    public static void init(BlockClient bc) {
        bc.FS_init();
    }
}
