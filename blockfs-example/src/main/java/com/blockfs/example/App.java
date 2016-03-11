package com.blockfs.example;

import com.beust.jcommander.JCommander;
import com.blockfs.client.*;
import com.blockfs.example.commands.GetCommand;
import com.blockfs.example.commands.InitCommand;
import com.blockfs.example.commands.PutCommand;

import java.io.*;
import java.util.Scanner;

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
                if(get.start != -1) {
                    System.out.println(new String(getBytes(blockClient, get.hash.get(0), get.start, get.size)));
                }else {
                    getToFile(blockClient, get.hash.get(0), get.out);
                }
                break;
            case "put":
                if(put.start != -1) {
                    Scanner scanner = new Scanner(System.in);
                    String data = scanner.nextLine();
                    System.out.println(putBytes(blockClient, data, put.start, put.user, put.password));
                }else {
                    System.out.println(putFile(blockClient, put.filename.get(0), put.user, put.password));
                }
                break;
            case "init":
                init(blockClient, init.user, init.password);
                break;
        }

    }

    public static void getToFile(BlockClient bc, String hash, String outfile) {

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
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error writing to output file.");
        } catch (IBlockServerRequests.IntegrityException e) {
            System.out.println("Data integrity failed.");
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        }


    }

    public static String putFile(BlockClient bc, String filename, String user, String password) {

        String hash = null;

        try {
            File file = new File(filename);
            FileInputStream is = new FileInputStream(file);

            byte[] chunk = new byte[8192];
            int chunkLen = 0;
            int totalSize = 0;

            hash = bc.FS_init(user, password);

            while ((chunkLen = is.read(chunk)) != -1) {
                bc.FS_write(totalSize, chunkLen, chunk);
                totalSize += chunkLen;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error reading input file.");
        } catch (IBlockClient.UninitializedFSException e) {
            System.out.println("Filesystem not initialized.");
        } catch (IBlockServerRequests.IntegrityException e) {
            System.out.println("Data integrity failed.");
        } catch (WrongPasswordException e) {
            System.out.println("Wrong password!");
        } catch (ClientProblemException e) {
            System.out.println("Error. Client problem.");
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        }

        return hash;
    }

    public static String putBytes(BlockClient bc, String data, int start, String user, String password) {

        byte[] byteData = data.getBytes();
        String hash = "";

        try {
            hash = bc.FS_init(user, password);
            bc.FS_write(start, byteData.length, byteData);
        } catch (WrongPasswordException e) {
            System.out.println("Wrong password!");
        } catch (IBlockClient.UninitializedFSException e) {
            System.out.println("Filesystem not initialized.");
        } catch (IBlockServerRequests.IntegrityException e) {
            System.out.println("Data integrity error.");
        } catch (ClientProblemException e) {
            System.out.println("Error. Client problem.");
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        }


        return hash;

    }

    public static byte[] getBytes(BlockClient bc, String hash, int start, int size) {

        byte[] data = new byte[size];

        try {
            bc.FS_read(hash, start, size, data);
        } catch (IBlockServerRequests.IntegrityException e) {
            System.out.println("Data integrity error.");
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        }

        return data;
    }

    public static void init(BlockClient bc, String user, String password) {
        try {
            bc.FS_init(user, password);
        } catch (WrongPasswordException e) {
            System.out.println("Wrong password!");
        } catch (ClientProblemException e) {
            System.out.println("Error. Client problem.");
        }
    }
}
