package com.blockfs.example.commands;

import com.beust.jcommander.Parameter;

public class MainCommand {
    @Parameter(names = "--help", help = true)
    public boolean help;
}
