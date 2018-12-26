package com.credits.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class ObjectInputStreamWithClassLoader extends ObjectInputStream {

    private ClassLoader customLoader;

    public ObjectInputStreamWithClassLoader(InputStream in, ClassLoader customLoader) throws IOException {
        super(in);
        if (customLoader == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.customLoader = customLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
        throws ClassNotFoundException
    {
        String name = desc.getName();
        return Class.forName(name, false, this.customLoader);
    }
}
