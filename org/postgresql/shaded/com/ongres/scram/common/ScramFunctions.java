// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common;

import java.util.Arrays;
import org.postgresql.shaded.com.ongres.scram.common.util.CryptoUtil;
import java.nio.charset.StandardCharsets;
import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;

public class ScramFunctions
{
    private static final byte[] CLIENT_KEY_HMAC_KEY;
    private static final byte[] SERVER_KEY_HMAC_KEY;
    
    public static byte[] saltedPassword(final ScramMechanism scramMechanism, final StringPreparation stringPreparation, final String password, final byte[] salt, final int iteration) {
        return scramMechanism.saltedPassword(stringPreparation, password, salt, iteration);
    }
    
    public static byte[] hmac(final ScramMechanism scramMechanism, final byte[] message, final byte[] key) {
        return scramMechanism.hmac(key, message);
    }
    
    public static byte[] clientKey(final ScramMechanism scramMechanism, final byte[] saltedPassword) {
        return hmac(scramMechanism, ScramFunctions.CLIENT_KEY_HMAC_KEY, saltedPassword);
    }
    
    public static byte[] clientKey(final ScramMechanism scramMechanism, final StringPreparation stringPreparation, final String password, final byte[] salt, final int iteration) {
        return clientKey(scramMechanism, saltedPassword(scramMechanism, stringPreparation, password, salt, iteration));
    }
    
    public static byte[] serverKey(final ScramMechanism scramMechanism, final byte[] saltedPassword) {
        return hmac(scramMechanism, ScramFunctions.SERVER_KEY_HMAC_KEY, saltedPassword);
    }
    
    public static byte[] serverKey(final ScramMechanism scramMechanism, final StringPreparation stringPreparation, final String password, final byte[] salt, final int iteration) {
        return serverKey(scramMechanism, saltedPassword(scramMechanism, stringPreparation, password, salt, iteration));
    }
    
    public static byte[] hash(final ScramMechanism scramMechanism, final byte[] value) {
        return scramMechanism.digest(value);
    }
    
    public static byte[] storedKey(final ScramMechanism scramMechanism, final byte[] clientKey) {
        return hash(scramMechanism, clientKey);
    }
    
    public static byte[] clientSignature(final ScramMechanism scramMechanism, final byte[] storedKey, final String authMessage) {
        return hmac(scramMechanism, authMessage.getBytes(StandardCharsets.UTF_8), storedKey);
    }
    
    public static byte[] clientProof(final byte[] clientKey, final byte[] clientSignature) {
        return CryptoUtil.xor(clientKey, clientSignature);
    }
    
    public static byte[] serverSignature(final ScramMechanism scramMechanism, final byte[] serverKey, final String authMessage) {
        return clientSignature(scramMechanism, serverKey, authMessage);
    }
    
    public static boolean verifyClientProof(final ScramMechanism scramMechanism, final byte[] clientProof, final byte[] storedKey, final String authMessage) {
        final byte[] clientSignature = clientSignature(scramMechanism, storedKey, authMessage);
        final byte[] clientKey = CryptoUtil.xor(clientSignature, clientProof);
        final byte[] computedStoredKey = hash(scramMechanism, clientKey);
        return Arrays.equals(storedKey, computedStoredKey);
    }
    
    public static boolean verifyServerSignature(final ScramMechanism scramMechanism, final byte[] serverKey, final String authMessage, final byte[] serverSignature) {
        return Arrays.equals(serverSignature(scramMechanism, serverKey, authMessage), serverSignature);
    }
    
    static {
        CLIENT_KEY_HMAC_KEY = "Client Key".getBytes(StandardCharsets.UTF_8);
        SERVER_KEY_HMAC_KEY = "Server Key".getBytes(StandardCharsets.UTF_8);
    }
}
