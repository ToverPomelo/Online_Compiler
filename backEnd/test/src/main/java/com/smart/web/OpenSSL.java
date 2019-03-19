/*
    Description: Methods for encrypt and decrypt.
*/

package com.smart.web;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class OpenSSL {
    private static PrivateKey priKey;
    private static PublicKey pubKey;

    public OpenSSL(){
        priKey = null;
        pubKey = null;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static void loadPrivateKey(String path) throws Exception{
        byte[] keyBytes = Files.readAllBytes(Paths.get(path));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        priKey = kf.generatePrivate(spec);
    }

    public static boolean hasPriKey(){
        return priKey != null;
    }

    public static void loadPublicKey(String path) throws Exception{
        byte[] keyBytes = Files.readAllBytes(Paths.get(path));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        pubKey = kf.generatePublic(spec);
    }

    public static boolean hasPubKey(){
        return pubKey != null;
    }

    public static String getPubKey() throws Exception{
        if(!hasPubKey()) throw new Exception("No public key");
        //System.out.println("key: " + pubKey);
        return bytesToHex(pubKey.getEncoded());
    }

    public static byte[] encrypt(String message) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        if(!hasPubKey()) throw new Exception("No public key");
        cipher.init(Cipher.ENCRYPT_MODE,pubKey);
        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        if(!hasPriKey()) throw new Exception("No private key");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return cipher.doFinal(encrypted);
    }
}
