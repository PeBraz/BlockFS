package com.blockfs.example.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription="Init FS")
public class InitCommand {
    @Parameter(names = "--help", help = true)
    public boolean help;

    @Parameter(names = "--user", description = "user")
    public String user;

    @Parameter(names = "--p", description = "user password", password = true)
    public String password;

}
