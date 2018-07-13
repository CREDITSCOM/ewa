package com.credits.thrift;

import com.credits.exception.UnsupportedTypeException;
import com.credits.thrift.generated.Variant;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VariantMapper implements Function<Object, Optional<Variant>> {

    @Override
    public Optional<Variant> apply(Object o) {
        try {
            return Optional.of(map(o));
        } catch (UnsupportedTypeException e) {
            return Optional.empty();
        }
    }
    
    @SuppressWarnings("unchecked")
    private Variant map(Object object) throws UnsupportedTypeException {
        Variant variant;
        if (object == null) {
            variant = null;
        } else if (object instanceof List) {
            List<Variant> variantCollection =
                ((List<Object>) object).stream().map(this::mapSimpleType).collect(Collectors.toList());

            if (variantCollection.stream().anyMatch(Objects::isNull)) {
                throw new UnsupportedTypeException();
            }

            variant = new Variant(Variant._Fields.V_LIST, variantCollection);
        } else if (object instanceof Set) {
            Set<Variant> variantCollection =
                ((Set<Object>) object).stream().map(this::mapSimpleType).collect(Collectors.toSet());

            if (variantCollection.stream().anyMatch(Objects::isNull)) {
                throw new UnsupportedTypeException();
            }

            variant = new Variant(Variant._Fields.V_SET, variantCollection);
        } else if (object instanceof Map) {
            Map<Variant, Variant> variantMap = ((Map<Object, Object>) object).entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> mapSimpleType(entry.getKey()), entry -> mapSimpleType(entry.getValue())));

            boolean match = variantMap.entrySet()
                .stream()
                .anyMatch(vEntry -> Objects.isNull(vEntry.getKey()) || Objects.isNull(vEntry.getValue()));
            if (match) {
                throw new UnsupportedTypeException();
            }

            variant = new Variant(Variant._Fields.V_MAP, variantMap);
        } else {
            variant = mapSimpleType(object);
        }

        return variant;
    }

    /**
     * Returns null for unsupported
     *
     * @param object an object to map
     * @return Thrift custor type defined as Variant
     */
    private Variant mapSimpleType(Object object) {
        Variant variant;
        if (object instanceof Boolean) {
            variant = new Variant(Variant._Fields.V_BOOL, object);
        } else if (object instanceof Byte) {
            variant = new Variant(Variant._Fields.V_I8, object);
        } else if (object instanceof Short) {
            variant = new Variant(Variant._Fields.V_I16, object);
        } else if (object instanceof Integer) {
            variant = new Variant(Variant._Fields.V_I32, object);
        } else if (object instanceof Long) {
            variant = new Variant(Variant._Fields.V_I64, object);
        } else if (object instanceof Double) {
            variant = new Variant(Variant._Fields.V_DOUBLE, object);
        } else if (object instanceof String) {
            variant = new Variant(Variant._Fields.V_STRING, object);
        } else {
            return null;
        }
        return variant;
    }
}
