package com.blockfs.server;

public class BlockFS implements IBlockServer
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }

    public byte[] get(String id) {
        return new byte[0];
    }

    public String put_k(byte[] data, String signature, String publicKey) {
        // faz hash da data
        // verifica assinatura com publoc key
        // compara hashs
        //excepç\ao e cenas se nao 401
        //

        return null;
    }

    public String put_h(byte[] data) {
        return null;
    }
}
