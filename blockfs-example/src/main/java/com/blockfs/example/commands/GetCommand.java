package com.blockfs.example.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription="Get file from server")
public class GetCommand {

    @Parameter(names = "--start", description = "Position to start reading from")
    public int start;

    @Parameter(names = "--size", description = "Number of bytes to read")
    public int size;

    @Parameter(description = "File to read from")
    public List<String> hash;

}
