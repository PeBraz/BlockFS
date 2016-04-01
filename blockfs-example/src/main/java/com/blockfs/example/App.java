package com.blockfs.example;

import com.beust.jcommander.JCommander;
import com.blockfs.client.*;
import com.blockfs.client.exception.*;
import com.blockfs.example.commands.GetCommand;
import com.blockfs.example.commands.InitCommand;
import com.blockfs.example.commands.ListCommand;
import com.blockfs.example.commands.PutCommand;


import java.io.*;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class App
{

    private static int CHUNKSIZE = 8192;

    public static void main( String[] args )
    {

        CCBlockClient blockClient = new CCBlockClient();

        JCommander jc = new JCommander();
        GetCommand get = new GetCommand();
        PutCommand put = new PutCommand();
        InitCommand init = new InitCommand();
        ListCommand list = new ListCommand();

        jc.addCommand("get", get);
        jc.addCommand("put", put);
        jc.addCommand("init", init);
        jc.addCommand("list", list);

        jc.parse(args);
        switch(jc.getParsedCommand()) {
            case "get":
                if(get.start != -1) {
                    System.out.println(new String(getBytes(blockClient, Integer.parseInt(get.pkey.get(0)), get.start, get.size)));
                }else {
                    getToFile(blockClient, Integer.parseInt(get.pkey.get(0)), get.out);
                }
                break;
            case "put":
                if(put.start != -1) {
                    Scanner scanner = new Scanner(System.in);
                    String data = scanner.nextLine();
                    putBytes(blockClient, data, put.start);
                }else {
                    putFile(blockClient, put.filename.get(0));
                }
                break;
            case "init":
                init(blockClient);
                System.out.println("called init: success");
                break;
            case "list":
                list(blockClient);
                break;
        }

    }

    public static void getToFile(CCBlockClient bc, int pkey, String outfile) {

        try {
            File file = new File(outfile);
            FileOutputStream os = new FileOutputStream(file, true);
            PublicKey publickey = bc.FS_list().get(pkey);

            byte[] chunk = new byte[CHUNKSIZE];
            int chunkLen = 0;
            int totalSize = 0;

            while ((chunkLen = bc.FS_read(publickey, totalSize, CHUNKSIZE, chunk)) == 8192) {
                os.write(chunk, 0, chunkLen);
                chunk = new byte[CHUNKSIZE];
                totalSize += chunkLen;
            }

            chunk = new byte[CHUNKSIZE];
            chunkLen = bc.FS_read(publickey, totalSize, CHUNKSIZE, chunk);
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
        } catch (InvalidCertificate invalidCertificate) {
            System.out.println("Invalid Certificate received.");
        }


    }

    public static void putFile(CCBlockClient bc, String filename) {

        try {
            File file = new File(filename);
            FileInputStream is = new FileInputStream(file);
            bc.FS_init();

            byte[] chunk = new byte[8192];
            int chunkLen = 0;
            int totalSize = 0;

            while ((chunkLen = is.read(chunk)) != -1) {
                bc.FS_write(totalSize, chunkLen, chunk);
                totalSize += chunkLen;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println("Error reading input file.");
        } catch (IBlockServerRequests.IntegrityException e) {
            System.out.println("Data integrity failed.");
        } catch (ClientProblemException e) {
            System.out.println("Error. Client problem.");
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        } catch (ICCBlockClient.UninitializedFSException e) {
            e.printStackTrace();
        } catch (WrongCardPINException e) {
            e.printStackTrace();
        } catch (NoCardDetectedException e) {
            e.printStackTrace();
        }

    }

    public static void putBytes(CCBlockClient bc, String data, int start) {

        byte[] byteData = data.getBytes();

        try {
            bc.FS_init();
            bc.FS_write(start, byteData.length, byteData);
        } catch (IBlockServerRequests.IntegrityException e) {
            System.out.println("Data integrity error.");
        } catch (ClientProblemException e) {
            System.out.println("Error. Client problem.");
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        } catch (ICCBlockClient.UninitializedFSException e) {
            e.printStackTrace();
        } catch (WrongCardPINException e) {
            e.printStackTrace();
        } catch (NoCardDetectedException e) {
            e.printStackTrace();
        }

    }

    public static byte[] getBytes(CCBlockClient bc, int pkey, int start, int size) {

        byte[] data = new byte[size];

        try {
            PublicKey publickey = bc.FS_list().get(pkey);
            bc.FS_read(publickey, start, size, data);
        } catch (IBlockServerRequests.IntegrityException e) {
            System.out.println("Data integrity error.");
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        } catch (InvalidCertificate invalidCertificate) {
            System.out.println("Invalid Certificate received.");
        }

        return data;
    }

    public static void init(CCBlockClient bc) {
        try {
            bc.FS_init();
        } catch (NoCardDetectedException e) {
            e.printStackTrace();
        } catch (IBlockServerRequests.IntegrityException e) {
            e.printStackTrace();
        } catch (ServerRespondedErrorException e) {
            e.printStackTrace();
        }
    }

    public static void list(CCBlockClient bc) {
        try {
            List<PublicKey> pkeys = bc.FS_list();

            for(int i=0; i < pkeys.size(); i++) {
                System.out.println("\n" + i + ": " + Base64.getEncoder().encodeToString(pkeys.get(i).getEncoded()));
            }

        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        }catch (InvalidCertificate invalidCertificate) {
            System.out.println("Invalid Certificate received.");
        }
    }
}
