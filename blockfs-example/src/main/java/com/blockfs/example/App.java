package com.blockfs.example;

import com.beust.jcommander.JCommander;
import com.blockfs.client.BlockClient;
import com.blockfs.client.IBlockClient;
import com.blockfs.client.IBlockServerRequests;
import com.blockfs.example.commands.GetCommand;
import com.blockfs.example.commands.InitCommand;
import com.blockfs.example.commands.PutCommand;

import java.io.*;

public class App
{

    private static int CHUNKSIZE = 8192;

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
                get(blockClient, get.hash.get(0), get.out);
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

    public static void get(BlockClient bc, String hash, String outfile) {

        try {
            File file = new File(outfile);
            FileOutputStream os = new FileOutputStream(file, true);

            byte[] chunk = new byte[CHUNKSIZE];
            int chunkLen = 0;
            int totalSize = 0;

            while ((chunkLen = bc.FS_read(hash, totalSize, CHUNKSIZE, chunk)) == 8192) {
                os.write(chunk, 0, chunkLen);
                chunk = new byte[CHUNKSIZE];
                totalSize += chunkLen;
            }

            chunk = new byte[CHUNKSIZE];
            chunkLen = bc.FS_read(hash, totalSize, CHUNKSIZE, chunk);
            os.write(chunk, 0, chunkLen);
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
        }


    }

    public static String putFile(BlockClient bc, String filename) {

        String hash = null;

        try {
            File file = new File(filename);
            FileInputStream is = new FileInputStream(file);

            byte[] chunk = new byte[8192];
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
