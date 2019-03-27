package com.credits.general.util.variant;

import com.credits.general.exception.CreditsException;
import com.credits.general.thrift.generated.Variant;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.credits.general.serialize.Serializer.deserialize;
import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.general.thrift.generated.Variant._Fields.V_ARRAY;
import static com.credits.general.thrift.generated.Variant._Fields.V_BOOLEAN_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_BYTE;
import static com.credits.general.thrift.generated.Variant._Fields.V_BYTE_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_DOUBLE;
import static com.credits.general.thrift.generated.Variant._Fields.V_DOUBLE_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_FLOAT;
import static com.credits.general.thrift.generated.Variant._Fields.V_FLOAT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_INT;
import static com.credits.general.thrift.generated.Variant._Fields.V_INT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_LIST;
import static com.credits.general.thrift.generated.Variant._Fields.V_LONG;
import static com.credits.general.thrift.generated.Variant._Fields.V_LONG_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_MAP;
import static com.credits.general.thrift.generated.Variant._Fields.V_NULL;
import static com.credits.general.thrift.generated.Variant._Fields.V_OBJECT;
import static com.credits.general.thrift.generated.Variant._Fields.V_SET;
import static com.credits.general.thrift.generated.Variant._Fields.V_SHORT;
import static com.credits.general.thrift.generated.Variant._Fields.V_SHORT_BOX;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class VariantMapper {
    public static final Byte NULL_TYPE_VALUE = 0;
    public static final Byte VOID_TYPE_VALUE = 0;

    public static class ObjectToVariant implements Function<Object, Optional<Variant>> {

        @Override
        public Optional<Variant> apply(Object o) {
            return Optional.of(map(o));
        }

        public Optional<Variant> apply(byte val) {
            return Optional.of(new Variant(V_BYTE, val));
        }

        public Optional<Variant> apply(int val) {
            return Optional.of(new Variant(V_INT, val));
        }

        public Optional<Variant> apply(short val) {
            return Optional.of(new Variant(V_SHORT, val));
        }

        public Optional<Variant> apply(long val) {
            return Optional.of(new Variant(V_LONG, val));
        }

        public Optional<Variant> apply(float val) {
            return Optional.of(new Variant(V_FLOAT, (double) val));
        }

        public Optional<Variant> apply(double val) {
            return Optional.of(new Variant(V_DOUBLE, val));
        }

        @SuppressWarnings("unchecked")
        private Variant map(Object object) {
            Variant variant;
            if (object == null) {
                return new Variant(V_NULL, NULL_TYPE_VALUE);
            } else if (object.getClass().isArray()) {
                variant = mapArray(object);
            } else if (object instanceof List) {
                List<Variant> variantCollection =
                    ((List<Object>) object).stream().map(this::map).collect(Collectors.toList());
                variant = new Variant(V_LIST, variantCollection);
            } else if (object instanceof Set) {
                Set<Variant> variantCollection =
                    ((Set<Object>) object).stream().map(this::mapObject).collect(Collectors.toSet());
                variant = new Variant(V_SET, variantCollection);
            } else if (object instanceof Map) {
                Map<Variant, Variant> variantMap = ((Map<Object, Object>) object).entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> mapObject(entry.getKey()), entry -> mapObject(entry.getValue())));
                variant = new Variant(V_MAP, variantMap);
            } else {
                variant = mapObject(object);
            }
            return variant;
        }

        /**
         * Returns null for unsupported
         *
         * @param object an object to map
         * @return Thrift custom type defined as Variant
         */
        private Variant mapObject(Object object) {
            Variant variant;
            if (object instanceof Boolean) {
                variant = new Variant(V_BOOLEAN_BOX, object);
            } else if (object instanceof Byte) {
                variant = new Variant(V_BYTE_BOX, object);
            } else if (object instanceof Short) {
                variant = new Variant(V_SHORT_BOX, object);
            } else if (object instanceof Integer) {
                variant = new Variant(V_INT_BOX, object);
            } else if (object instanceof Long) {
                variant = new Variant(V_LONG_BOX, object);
            } else if (object instanceof Float) {
                variant = new Variant(V_FLOAT_BOX, (double) (float) object);
            } else if (object instanceof Double) {
                variant = new Variant(V_DOUBLE_BOX, object);
            } else if (object instanceof String) {
                variant = new Variant(V_STRING, object);
            } else {
                variant = new Variant(V_OBJECT, ByteBuffer.wrap(serialize(object)));
            }
            return variant;
        }

        private Variant mapArray(Object object) {
            List<Variant> variantCollection = new ArrayList<>();
            if (object instanceof Object[]) {
                variantCollection =
                    Arrays.stream((Object[]) object).map(this::map).collect(Collectors.toList());
            } else if (object instanceof byte[]) {
                for (byte b : (byte[]) object) {
                    variantCollection.add(new Variant(V_BYTE, b));
                }
            } else if (object instanceof int[]) {
                for (int i : (int[]) object) {
                    variantCollection.add(new Variant(V_INT, i));
                }
            } else if (object instanceof long[]) {
                for (long i : (long[]) object) {
                    variantCollection.add(new Variant(V_LONG, i));
                }
            } else if (object instanceof short[]) {
                for (short s : (short[]) object) {
                    variantCollection.add(new Variant(V_SHORT, s));
                }
            } else if (object instanceof float[]) {
                for (float f : (float[]) object) {
                    variantCollection.add(new Variant(V_FLOAT, f));
                }
            } else if (object instanceof double[]) {
                for (double d : (double[]) object) {
                    variantCollection.add(new Variant(V_DOUBLE, d));
                }
            }

            if (variantCollection.isEmpty()) {
                throw new CreditsException(
                    String.format("Unsupported object type: %s", object.getClass().getSimpleName()));
            }
            return new Variant(V_ARRAY, variantCollection);
        }

    }

    public static class VariantToObject implements BiFunction<Variant, ClassLoader[], Object> {

        @Override
        public Object apply(Variant variant, ClassLoader... classLoader) {
            return map(variant, classLoader);
        }

        @SuppressWarnings("unchecked")
        private Object map(Variant variant, ClassLoader... classLoader) {
            switch (variant.getSetField()) {
                case V_NULL:
                    return null;
                case V_VOID:
                    return Void.TYPE;
                case V_FLOAT:
                case V_FLOAT_BOX:
                    return (float) (double) variant.getFieldValue();
                case V_ARRAY:
                    return ((Collection<Variant>) variant.getFieldValue()).stream().map(this::map).toArray(Object[]::new);
                case V_LIST:
                    return ((Collection<Variant>) variant.getFieldValue()).stream().map(this::map).collect(toList());
                case V_SET:
                    return ((Collection<Variant>) variant.getFieldValue()).stream().map(this::map).collect(toSet());
                case V_MAP:
                    Map<Object, Object> objectMap = new HashMap<>();
                    for (Map.Entry<Variant, Variant> entry : ((Map<Variant, Variant>) variant.getFieldValue()).entrySet()) {
                        objectMap.put(map(entry.getKey()), map(entry.getValue()));
                    }
                    return objectMap;
                case V_OBJECT:
                    return deserialize(variant.getV_object(), classLoader.length > 0 ? classLoader[0] : getClass().getClassLoader());
                default:
                    return variant.getFieldValue();
            }
        }

    }
}
