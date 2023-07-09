// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.client;

import org.postgresql.shaded.com.ongres.scram.common.util.CryptoUtil;
import java.security.GeneralSecurityException;
import java.security.NoSuchProviderException;
import java.security.NoSuchAlgorithmException;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2CbindFlag;
import org.postgresql.shaded.com.ongres.scram.common.ScramMechanisms;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import java.security.SecureRandom;
import org.postgresql.shaded.com.ongres.scram.common.ScramMechanism;
import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;

public class ScramClient
{
    public static final int DEFAULT_NONCE_LENGTH = 24;
    private final ChannelBinding channelBinding;
    private final StringPreparation stringPreparation;
    private final ScramMechanism scramMechanism;
    private final SecureRandom secureRandom;
    private final NonceSupplier nonceSupplier;
    
    private ScramClient(final ChannelBinding channelBinding, final StringPreparation stringPreparation, final ScramMechanism nonChannelBindingMechanism, final ScramMechanism channelBindingMechanism, final SecureRandom secureRandom, final NonceSupplier nonceSupplier) {
        assert null != channelBinding : "channelBinding";
        assert null != stringPreparation : "stringPreparation";
        assert null != channelBindingMechanism : "Either a channel-binding or a non-binding mechanism must be present";
        assert null != secureRandom : "secureRandom";
        assert null != nonceSupplier : "nonceSupplier";
        this.channelBinding = channelBinding;
        this.stringPreparation = stringPreparation;
        this.scramMechanism = ((null != nonChannelBindingMechanism) ? nonChannelBindingMechanism : channelBindingMechanism);
        this.secureRandom = secureRandom;
        this.nonceSupplier = nonceSupplier;
    }
    
    public static PreBuilder1 channelBinding(final ChannelBinding channelBinding) throws IllegalArgumentException {
        return new PreBuilder1((ChannelBinding)Preconditions.checkNotNull(channelBinding, "channelBinding"));
    }
    
    public StringPreparation getStringPreparation() {
        return this.stringPreparation;
    }
    
    public ScramMechanism getScramMechanism() {
        return this.scramMechanism;
    }
    
    public static List<String> supportedMechanisms() {
        final List<String> supportedMechanisms = new ArrayList<String>();
        for (final ScramMechanisms scramMechanisms : ScramMechanisms.values()) {
            supportedMechanisms.add(scramMechanisms.getName());
        }
        return supportedMechanisms;
    }
    
    public ScramSession scramSession(final String user) {
        return new ScramSession(this.scramMechanism, this.stringPreparation, Preconditions.checkNotEmpty(user, "user"), this.nonceSupplier.get());
    }
    
    public enum ChannelBinding
    {
        NO(Gs2CbindFlag.CLIENT_NOT), 
        YES(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED), 
        IF_SERVER_SUPPORTS_IT(Gs2CbindFlag.CLIENT_YES_SERVER_NOT);
        
        private final Gs2CbindFlag gs2CbindFlag;
        
        private ChannelBinding(final Gs2CbindFlag gs2CbindFlag) {
            this.gs2CbindFlag = gs2CbindFlag;
        }
        
        public Gs2CbindFlag gs2CbindFlag() {
            return this.gs2CbindFlag;
        }
    }
    
    public static class PreBuilder1
    {
        protected final ChannelBinding channelBinding;
        
        private PreBuilder1(final ChannelBinding channelBinding) {
            this.channelBinding = channelBinding;
        }
        
        public PreBuilder2 stringPreparation(final StringPreparation stringPreparation) throws IllegalArgumentException {
            return new PreBuilder2(this.channelBinding, (StringPreparation)Preconditions.checkNotNull(stringPreparation, "stringPreparation"));
        }
    }
    
    public static class PreBuilder2 extends PreBuilder1
    {
        protected final StringPreparation stringPreparation;
        protected ScramMechanism nonChannelBindingMechanism;
        protected ScramMechanism channelBindingMechanism;
        
        private PreBuilder2(final ChannelBinding channelBinding, final StringPreparation stringPreparation) {
            super(channelBinding);
            this.nonChannelBindingMechanism = null;
            this.channelBindingMechanism = null;
            this.stringPreparation = stringPreparation;
        }
        
        public Builder selectMechanismBasedOnServerAdvertised(final String... serverMechanisms) {
            Preconditions.checkArgument(null != serverMechanisms && serverMechanisms.length > 0, "serverMechanisms");
            this.nonChannelBindingMechanism = ScramMechanisms.selectMatchingMechanism(false, serverMechanisms);
            if (this.channelBinding == ChannelBinding.NO && null == this.nonChannelBindingMechanism) {
                throw new IllegalArgumentException("Server does not support non channel binding mechanisms");
            }
            this.channelBindingMechanism = ScramMechanisms.selectMatchingMechanism(true, serverMechanisms);
            if (this.channelBinding == ChannelBinding.YES && null == this.channelBindingMechanism) {
                throw new IllegalArgumentException("Server does not support channel binding mechanisms");
            }
            if (null == this.channelBindingMechanism && null == this.nonChannelBindingMechanism) {
                throw new IllegalArgumentException("There are no matching mechanisms between client and server");
            }
            return new Builder(this.channelBinding, this.stringPreparation, this.nonChannelBindingMechanism, this.channelBindingMechanism);
        }
        
        public Builder selectMechanismBasedOnServerAdvertisedCsv(final String serverMechanismsCsv) throws IllegalArgumentException {
            return this.selectMechanismBasedOnServerAdvertised(Preconditions.checkNotNull(serverMechanismsCsv, "serverMechanismsCsv").split(","));
        }
        
        public Builder selectClientMechanism(final ScramMechanism scramMechanism) {
            Preconditions.checkNotNull(scramMechanism, "scramMechanism");
            if (this.channelBinding == ChannelBinding.IF_SERVER_SUPPORTS_IT) {
                throw new IllegalArgumentException("If server selection is considered, no direct client selection should be performed");
            }
            if ((this.channelBinding == ChannelBinding.YES && !scramMechanism.supportsChannelBinding()) || (this.channelBinding == ChannelBinding.NO && scramMechanism.supportsChannelBinding())) {
                throw new IllegalArgumentException("Incompatible selection of mechanism and channel binding");
            }
            if (scramMechanism.supportsChannelBinding()) {
                return new Builder(this.channelBinding, this.stringPreparation, (ScramMechanism)null, scramMechanism);
            }
            return new Builder(this.channelBinding, this.stringPreparation, scramMechanism, (ScramMechanism)null);
        }
    }
    
    public static class Builder extends PreBuilder2
    {
        private final ScramMechanism nonChannelBindingMechanism;
        private final ScramMechanism channelBindingMechanism;
        private SecureRandom secureRandom;
        private NonceSupplier nonceSupplier;
        private int nonceLength;
        
        private Builder(final ChannelBinding channelBinding, final StringPreparation stringPreparation, final ScramMechanism nonChannelBindingMechanism, final ScramMechanism channelBindingMechanism) {
            super(channelBinding, stringPreparation);
            this.secureRandom = new SecureRandom();
            this.nonceLength = 24;
            this.nonChannelBindingMechanism = nonChannelBindingMechanism;
            this.channelBindingMechanism = channelBindingMechanism;
        }
        
        public Builder secureRandomAlgorithmProvider(final String algorithm, final String provider) throws IllegalArgumentException {
            Preconditions.checkNotNull(algorithm, "algorithm");
            try {
                this.secureRandom = ((null == provider) ? SecureRandom.getInstance(algorithm) : SecureRandom.getInstance(algorithm, provider));
            }
            catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new IllegalArgumentException("Invalid algorithm or provider", e);
            }
            return this;
        }
        
        public Builder nonceSupplier(final NonceSupplier nonceSupplier) throws IllegalArgumentException {
            this.nonceSupplier = Preconditions.checkNotNull(nonceSupplier, "nonceSupplier");
            return this;
        }
        
        public Builder nonceLength(final int length) throws IllegalArgumentException {
            this.nonceLength = Preconditions.gt0(length, "length");
            return this;
        }
        
        public ScramClient setup() {
            return new ScramClient(this.channelBinding, this.stringPreparation, this.nonChannelBindingMechanism, this.channelBindingMechanism, this.secureRandom, (this.nonceSupplier != null) ? this.nonceSupplier : new NonceSupplier() {
                @Override
                public String get() {
                    return CryptoUtil.nonce(Builder.this.nonceLength, Builder.this.secureRandom);
                }
            }, null);
        }
    }
}
