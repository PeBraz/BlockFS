package com.blockfs.example.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription="Get file from server")
public class GetCommand {

    @Parameter(names = "--out", description = "File to write to")
    public String out;

    @Parameter(names = "--key", description = "File id (public key) to fetch")
    public int pkey;

    @Parameter(names = "--size", description = "Number of bytes to read")
    public int size = -1;

    @Parameter(names = "--start", description = "Offset to start reading")
    public int start = -1;

    @Parameter(names = "--help", help = true)
    public boolean help;

}
