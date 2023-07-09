// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.retry;

import org.slf4j.LoggerFactory;
import java.util.function.Function;
import java.util.concurrent.Callable;
import org.slf4j.Logger;

public class RetryFunction<R>
{
    private static final Logger log;
    private static final int DEFAULT_MAX_TRIES = 10;
    private final int tries;
    private final String name;
    
    public RetryFunction() {
        this(10, "unnamed");
    }
    
    public RetryFunction(final String name) {
        this(10, name);
    }
    
    public RetryFunction(final int tries, final String name) {
        this.tries = tries;
        this.name = name;
    }
    
    public R execute(final Callable<R> function, final Function<R, Boolean> check, final Runnable finalise) {
        int iterator = this.tries;
        while (iterator > 0) {
            try {
                final R result = function.call();
                if (check.apply(result)) {
                    return result;
                }
                continue;
            }
            catch (final RuntimeException e) {
                RetryFunction.log.warn("couldn't successfully execute function " + this.name, (Throwable)e);
            }
            catch (final Exception e2) {
                RetryFunction.log.warn("couldn't successfully execute function " + this.name, (Throwable)e2);
            }
            finally {
                --iterator;
            }
        }
        finalise.run();
        throw new RuntimeException(String.format("Couldn't execute function %s, tried %d times.", function.toString(), this.tries));
    }
    
    public R execute(final Callable<R> function) {
        return (R)this.execute((Callable<Object>)function, result -> true, () -> {});
    }
    
    public R execute(final Callable<R> function, final Runnable finalise) {
        return (R)this.execute((Callable<Object>)function, result -> true, finalise);
    }
    
    public R execute(final Callable<R> function, final Function<R, Boolean> check) {
        return this.execute(function, check, () -> {});
    }
    
    static {
        log = LoggerFactory.getLogger((Class)RetryFunction.class);
    }
}
