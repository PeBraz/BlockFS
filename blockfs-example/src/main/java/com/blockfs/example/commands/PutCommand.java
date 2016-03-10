package com.blockfs.example.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription="Get file from server")
public class PutCommand {

    @Parameter(description = "File to read from")
    public List<String> filename;

}
