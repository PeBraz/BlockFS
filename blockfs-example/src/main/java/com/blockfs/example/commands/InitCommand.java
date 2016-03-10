package com.blockfs.example.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription="Init FS")
public class InitCommand {
    @Parameter(names = "--user", description = "User")
    public String user;

    @Parameter(names = "--p", description = "User password", password = true)
    public String password;

    @Parameter(names = "--help", help = true)
    public boolean help;
}
