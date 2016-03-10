package com.blockfs.example.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription="Get file from server")
public class GetCommand {

    @Parameter(names = "--out", description = "File to write to")
    public String out;

    @Parameter(description = "File hash to fetch")
    public List<String> hash;

}
