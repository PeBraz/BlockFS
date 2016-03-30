package com.blockfs.server.rest.model;


import java.util.List;

public class PKData {

    private int sequence;
    private List<String> hashes;

    public PKData(int sequence, List<String> hashes) {
        this.sequence = sequence;
        this.hashes = hashes;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public List<String> getHashes() {
        return hashes;
    }

    public void setHashes(List<String> hashes) {
        this.hashes = hashes;
    }
}
