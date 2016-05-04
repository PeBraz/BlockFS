package com.blockfs.server.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

public class DataBlock {

    public static void writeBlock(byte[] data, String hash, int port) {

        try {
            Path dir = Paths.get("./data/"+port);
            Files.createDirectories(dir);


            Path file = Paths.get("./data/"+port, hash);
            Files.write(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(InvalidPathException e) {
            e.printStackTrace();
        }

    }

    public static byte[] readBlock(String hash, int port) throws FileNotFoundException {

        try {
            System.out.println("readBlock:"+hash);
            Path file = Paths.get("./data/"+port, hash);
            return Files.readAllBytes(file);
        } catch (NoSuchFileException e){
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }
}
