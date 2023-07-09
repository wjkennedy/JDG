// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common;

import java.util.HashMap;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.KeyParameter;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.PBEParametersGenerator;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.PKCS5S2ParametersGenerator;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.DigestFactory;
import javax.crypto.SecretKeyFactory;
import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;
import org.postgresql.shaded.com.ongres.scram.common.util.CryptoUtil;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import java.util.Map;

public enum ScramMechanisms implements ScramMechanism
{
    SCRAM_SHA_1("SHA-1", "SHA-1", 160, "HmacSHA1", false, 1), 
    SCRAM_SHA_1_PLUS("SHA-1", "SHA-1", 160, "HmacSHA1", true, 1), 
    SCRAM_SHA_256("SHA-256", "SHA-256", 256, "HmacSHA256", false, 10), 
    SCRAM_SHA_256_PLUS("SHA-256", "SHA-256", 256, "HmacSHA256", true, 10);
    
    private static final String SCRAM_MECHANISM_NAME_PREFIX = "SCRAM-";
    private static final String CHANNEL_BINDING_SUFFIX = "-PLUS";
    private static final String PBKDF2_PREFIX_ALGORITHM_NAME = "PBKDF2With";
    private static final Map<String, ScramMechanisms> BY_NAME_MAPPING;
    private final String mechanismName;
    private final String hashAlgorithmName;
    private final int keyLength;
    private final String hmacAlgorithmName;
    private final boolean channelBinding;
    private final int priority;
    
    private ScramMechanisms(final String name, final String hashAlgorithmName, final int keyLength, final String hmacAlgorithmName, final boolean channelBinding, final int priority) {
        this.mechanismName = "SCRAM-" + Preconditions.checkNotNull(name, "name") + (channelBinding ? "-PLUS" : "");
        this.hashAlgorithmName = Preconditions.checkNotNull(hashAlgorithmName, "hashAlgorithmName");
        this.keyLength = Preconditions.gt0(keyLength, "keyLength");
        this.hmacAlgorithmName = Preconditions.checkNotNull(hmacAlgorithmName, "hmacAlgorithmName");
        this.channelBinding = channelBinding;
        this.priority = Preconditions.gt0(priority, "priority");
    }
    
    protected String getHashAlgorithmName() {
        return this.hashAlgorithmName;
    }
    
    protected String getHmacAlgorithmName() {
        return this.hmacAlgorithmName;
    }
    
    @Override
    public String getName() {
        return this.mechanismName;
    }
    
    @Override
    public boolean supportsChannelBinding() {
        return this.channelBinding;
    }
    
    @Override
    public int algorithmKeyLength() {
        return this.keyLength;
    }
    
    @Override
    public byte[] digest(final byte[] message) {
        try {
            return MessageDigest.getInstance(this.hashAlgorithmName).digest(message);
        }
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + this.hashAlgorithmName + " not present in current JVM");
        }
    }
    
    @Override
    public byte[] hmac(final byte[] key, final byte[] message) {
        try {
            return CryptoUtil.hmac(new SecretKeySpec(key, this.hmacAlgorithmName), Mac.getInstance(this.hmacAlgorithmName), message);
        }
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("MAC Algorithm " + this.hmacAlgorithmName + " not present in current JVM");
        }
    }
    
    @Override
    public byte[] saltedPassword(final StringPreparation stringPreparation, final String password, final byte[] salt, final int iterations) {
        final char[] normalizedString = stringPreparation.normalize(password).toCharArray();
        try {
            return CryptoUtil.hi(SecretKeyFactory.getInstance("PBKDF2With" + this.hmacAlgorithmName), this.algorithmKeyLength(), normalizedString, salt, iterations);
        }
        catch (final NoSuchAlgorithmException e) {
            if (!ScramMechanisms.SCRAM_SHA_256.getHmacAlgorithmName().equals(this.getHmacAlgorithmName())) {
                throw new RuntimeException("Unsupported PBKDF2 for " + this.mechanismName);
            }
            final PBEParametersGenerator generator = new PKCS5S2ParametersGenerator(DigestFactory.createSHA256());
            generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(normalizedString), salt, iterations);
            final KeyParameter params = (KeyParameter)generator.generateDerivedParameters(this.algorithmKeyLength());
            return params.getKey();
        }
    }
    
    public static ScramMechanisms byName(final String name) {
        Preconditions.checkNotNull(name, "name");
        return ScramMechanisms.BY_NAME_MAPPING.get(name);
    }
    
    public static ScramMechanism selectMatchingMechanism(final boolean channelBinding, final String... peerMechanisms) {
        ScramMechanisms selectedScramMechanisms = null;
        for (final String peerMechanism : peerMechanisms) {
            final ScramMechanisms matchedScramMechanisms = ScramMechanisms.BY_NAME_MAPPING.get(peerMechanism);
            if (matchedScramMechanisms != null) {
                for (final ScramMechanisms candidateScramMechanisms : values()) {
                    if (channelBinding == candidateScramMechanisms.channelBinding && candidateScramMechanisms.mechanismName.equals(matchedScramMechanisms.mechanismName) && (selectedScramMechanisms == null || selectedScramMechanisms.priority < candidateScramMechanisms.priority)) {
                        selectedScramMechanisms = candidateScramMechanisms;
                    }
                }
            }
        }
        return selectedScramMechanisms;
    }
    
    private static Map<String, ScramMechanisms> valuesAsMap() {
        final Map<String, ScramMechanisms> mapScramMechanisms = new HashMap<String, ScramMechanisms>(values().length);
        for (final ScramMechanisms scramMechanisms : values()) {
            mapScramMechanisms.put(scramMechanisms.getName(), scramMechanisms);
        }
        return mapScramMechanisms;
    }
    
    static {
        BY_NAME_MAPPING = valuesAsMap();
    }
}
