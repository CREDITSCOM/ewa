package com.credits.classload;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ByteCodeContractClassLoader extends ClassLoader {
    private Map<String, ClassObject> classes = new HashMap<>();

    public Class<?> loadClass(String className, byte[] byteCode) {
        Class<?> clazz;
        try {
            clazz = findClass(className);
        } catch (ClassNotFoundException e) {
            clazz = defineClass(className, byteCode, 0, byteCode.length);
            classes.put(className, new ClassObject(className, byteCode, clazz));
        }
        return clazz;
    }


    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        return classes.containsKey(className)
            ? classes.get(className).clazz
            : getClass().getClassLoader().loadClass(className);
    }

    public byte[] lookupBytecode(String className) throws ClassNotFoundException {
        if (classes.containsKey(className)) {
            return classes.get(className).bytecode;
        } else {
            throw new ClassNotFoundException(className + " class not loaded");
        }
    }

    private static class ClassObject implements Serializable {
        private static final long serialVersionUID = -1615398392394106604L;
        private final String className;
        private final byte[] bytecode;
        private final Class<?> clazz;

        public ClassObject(String className, byte[] bytecode, Class<?> clazz) {
            this.className = className;
            this.bytecode = bytecode;
            this.clazz = clazz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ClassObject)) {
                return false;
            }
            ClassObject that = (ClassObject) o;
            return Objects.equals(className, that.className) &&
                Arrays.equals(bytecode, that.bytecode) &&
                Objects.equals(clazz, that.clazz);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(className, clazz);
            result = 31 * result + Arrays.hashCode(bytecode);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ClassObject{");
            sb.append("className='").append(className).append('\'');
            sb.append(", bytecode=").append(Arrays.toString(bytecode));
            sb.append(", clazz=").append(clazz);
            sb.append('}');
            return sb.toString();
        }
    }
}
