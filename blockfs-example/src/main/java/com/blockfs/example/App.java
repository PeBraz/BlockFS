package com.blockfs.example;

import com.beust.jcommander.JCommander;
import com.blockfs.client.CCBlockClient;
import com.blockfs.client.IBlockServerRequests;
import com.blockfs.client.ICCBlockClient;
import com.blockfs.client.exception.*;
import com.blockfs.example.commands.*;

import java.io.*;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class App
{

    private static int CHUNKSIZE = 8192;
    private static String username;
    private static String password;
    private static boolean isInit = false;

    public static void main( String[] args )
    {

        CCBlockClient blockClient = new CCBlockClient();

        JCommander jc = new JCommander();
        GetCommand get = new GetCommand();
        PutCommand put = new PutCommand();
        InitCommand init = new InitCommand();
        ListCommand list = new ListCommand();
        ExitCommand exit = new ExitCommand();


        Scanner scannerIn = new Scanner(System.in);
        String cmd = "";
        boolean run = true;
        System.out.println("Welcome to FileOS! - 3.0");
        while (run){
            try {
                jc = new JCommander();
                jc.addCommand("get", get);
                jc.addCommand("put", put);
                jc.addCommand("init", init);
                jc.addCommand("list", list);
                jc.addCommand("exit", exit);
                System.out.println("type a command:");
                cmd = scannerIn.nextLine();
                System.out.println("cmd:"+cmd);
                jc.parse(cmd.split(" "));
                switch(jc.getParsedCommand()) {
                    case "get":
                        if(!isInit){
                            System.out.println("Call init first!");
                            break;
                        }
                        if(get.start != -1) {
                            System.out.println(new String(getBytes(blockClient, get.pkey, get.start, get.size)));
                        }else {
                            getToFile(blockClient, get.pkey, get.out);
                        }
                        break;
                    case "put":
                        if(!isInit){
                            System.out.println("Call init first!");
                            break;
                        }
                        if(put.start != -1) {
                            Scanner scanner = new Scanner(System.in);
                            String data = scanner.nextLine();
                            putBytes(blockClient, data, put.start);
                        }else {
                            putFile(blockClient, put.filename.get(0));
                        }
                        break;
                    case "init":
                        init(blockClient, init.user, init.password);
                        System.out.println("called init: success");
                        break;
                    case "list":
                        if(!isInit){
                            System.out.println("Call init first!");
                            break;
                        }
                        list(blockClient);
                        break;
                    case "exit":
                        run = false;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        scannerIn.close();

        System.out.println("Bye bye!");


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
        }

    }

    public static void putBytes(CCBlockClient bc, String data, int start) {

        byte[] byteData = data.getBytes();

        try {

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

    public static void init(CCBlockClient bc, String ... args) {
        try {

            if (args.length > 0 && args[0] != null) {
                username =args[0];
                password = args[1];
                System.out.println("username:"+username);
                bc.FS_init(username, password);
            }
            else {
                bc.FS_init();
                username ="";
                password = "";
            }
            isInit = true;
        } catch (NoCardDetectedException e) {
            System.out.println("No card detected!");
            isInit = false;
        } catch (IBlockServerRequests.IntegrityException | ClientProblemException | ServerRespondedErrorException e) {
            e.printStackTrace();
            isInit = false;
        } catch (WrongPasswordException e) {
            System.out.println("Wrong password");
            isInit = false;
        }
    }

    public static void list(CCBlockClient bc) {
        try {
            List<PublicKey> pkeys = bc.FS_list();

            int myKey = -1;
            for(int i=0; i < pkeys.size(); i++) {
                System.out.println("\n" + i + ": " + Base64.getEncoder().encodeToString(pkeys.get(i).getEncoded()));

                if(myKey < 0 && Arrays.equals(pkeys.get(i).getEncoded(), bc.getPubKey().getEncoded()))
                    myKey = i;
            }
            System.out.println("\nYour key is number:"+myKey);
        } catch (ServerRespondedErrorException e) {
            System.out.println("Server error.");
        }catch (InvalidCertificate invalidCertificate) {
            System.out.println("Invalid Certificate received.");
        }
    }
}
