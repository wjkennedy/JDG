// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.gss;

import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.NameCallback;
import java.io.IOException;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public class GSSCallbackHandler implements CallbackHandler
{
    private final String user;
    private final String password;
    
    public GSSCallbackHandler(final String user, final String password) {
        this.user = user;
        this.password = password;
    }
    
    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (final Callback callback : callbacks) {
            if (callback instanceof TextOutputCallback) {
                final TextOutputCallback toc = (TextOutputCallback)callback;
                switch (toc.getMessageType()) {
                    case 0: {
                        System.out.println("INFO: " + toc.getMessage());
                        break;
                    }
                    case 2: {
                        System.out.println("ERROR: " + toc.getMessage());
                        break;
                    }
                    case 1: {
                        System.out.println("WARNING: " + toc.getMessage());
                        break;
                    }
                    default: {
                        throw new IOException("Unsupported message type: " + toc.getMessageType());
                    }
                }
            }
            else if (callback instanceof NameCallback) {
                final NameCallback nc = (NameCallback)callback;
                nc.setName(this.user);
            }
            else {
                if (!(callback instanceof PasswordCallback)) {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                }
                final PasswordCallback pc = (PasswordCallback)callback;
                if (this.password == null) {
                    throw new IOException("No cached kerberos ticket found and no password supplied.");
                }
                pc.setPassword(this.password.toCharArray());
            }
        }
    }
}
