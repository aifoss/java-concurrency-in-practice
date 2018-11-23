package org.concurrency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sofia on 5/25/17.
 */
public class Annotation {

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface NotThreadSafe {

    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface ThreadSafe {

    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public @interface Immutable {

    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface GuardedBy {
        String value();
    }

}
