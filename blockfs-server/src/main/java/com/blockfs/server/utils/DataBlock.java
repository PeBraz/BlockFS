package com.blockfs.server.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static byte[] readBlock(String hash) {

        Path file = Paths.get("./data", hash);

        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }
}
