// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2;

public abstract class PBEParametersGenerator
{
    protected byte[] password;
    protected byte[] salt;
    protected int iterationCount;
    
    protected PBEParametersGenerator() {
    }
    
    public void init(final byte[] password, final byte[] salt, final int iterationCount) {
        this.password = password;
        this.salt = salt;
        this.iterationCount = iterationCount;
    }
    
    public byte[] getPassword() {
        return this.password;
    }
    
    public byte[] getSalt() {
        return this.salt;
    }
    
    public int getIterationCount() {
        return this.iterationCount;
    }
    
    public abstract CipherParameters generateDerivedParameters(final int p0);
    
    public static byte[] PKCS5PasswordToUTF8Bytes(final char[] password) {
        if (password != null) {
            return Strings.toUTF8ByteArray(password);
        }
        return new byte[0];
    }
}
