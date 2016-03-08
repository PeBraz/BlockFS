package com.blockfs.server.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

public class DataBlock {

    public static void writeBlock(byte[] data, String hash) {

        try {
            Path file = Paths.get("./data", hash);
            Files.write(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(InvalidPathException e) {
            e.printStackTrace();
        }

    }

    public static byte[] readBlock(String hash) throws FileNotFoundException {



        try {
            System.out.println("readBlock:"+hash);
            Path file = Paths.get("./data", hash);
            return Files.readAllBytes(file);
        } catch (NoSuchFileException e){
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }
}
