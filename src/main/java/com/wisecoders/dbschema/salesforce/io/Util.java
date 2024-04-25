package com.wisecoders.dbschema.salesforce.io;

import com.wisecoders.dbschema.salesforce.JdbcDriver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/salesforce-jdbc-driver">public GIT repository</a>.
 */
public class Util {

    public static String md5Java(String message){
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));

            //converting byte array to Hexadecimal String
            StringBuilder sb = new StringBuilder(2*hash.length);
            for(byte b : hash){
                sb.append(String.format("%02x", b&0xff));
            }

            digest = sb.toString();

        } catch ( NoSuchAlgorithmException ex) {
            JdbcDriver.LOGGER.log(Level.SEVERE, null, ex);
        }
        return digest;
    }
}
