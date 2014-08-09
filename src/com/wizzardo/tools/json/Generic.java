package com.wizzardo.tools.json;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/21/14
 */
public class Generic<T> {
    final Class<T> clazz;
    final Generic[] typeParameters;
    final Generic parent;
    private Map<String, Generic> types;

    public Generic(Type c) {
        this(c, (Map) null);
    }

    public Generic(Class<T> c, Class... generics) {
        clazz = c;
        parent = null;
        if (generics == null) {
            typeParameters = new Generic[0];
            return;
        }
        typeParameters = new Generic[generics.length];
        for (int i = 0; i < generics.length; i++) {
            typeParameters[i] = new Generic(generics[i]);
        }

        types = getTypes(c, typeParameters);
    }

    public Generic(Class<T> c, Generic... generics) {
        clazz = c;
        parent = null;
        if (generics == null)
            typeParameters = new Generic[0];
        else
            typeParameters = generics;

        types = getTypes(c, typeParameters);
    }

    private Map<String, Generic> getTypes(Class<T> c, Generic[] generics) {
        TypeVariable<Class<T>>[] variables = clazz.getTypeParameters();
        if (variables.length == 0 || generics.length == 0)
            return null;

        Map<String, Generic> types = new HashMap<String, Generic>();
        for (int i = 0; i < variables.length && i < generics.length; i++) {
            types.put(variables[i].getName(), generics[i]);
        }
        return types;
    }

    Generic(Type c, Map<String, Generic> types) {
        if (c instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) c;
            clazz = (Class) type.getRawType();
            Type[] args = type.getActualTypeArguments();
            TypeVariable<Class<T>>[] variables = clazz.getTypeParameters();

            this.types = new HashMap<String, Generic>();
            this.typeParameters = new Generic[args.length];
            for (int i = 0; i < args.length; i++) {
                this.typeParameters[i] = new Generic(args[i], types);
                this.types.put(variables[i].getName(), this.typeParameters[i]);
            }

            if (clazz.getGenericSuperclass() != null)
                parent = new Generic(clazz.getGenericSuperclass(), this.types);
            else
                parent = null;
        } else if (c instanceof TypeVariable) {
            if (types != null) {
                Generic g = types.get(((TypeVariable) c).getName());
                clazz = g.clazz;
                parent = g.parent;
                typeParameters = g.typeParameters;
            } else {
                clazz = (Class<T>) Object.class;
                parent = null;
                typeParameters = new Generic[0];
            }
        } else if (c instanceof GenericArrayType) {
            parent = null;
            clazz = (Class<T>) Array.class;
            typeParameters = new Generic[]{new Generic(((GenericArrayType) c).getGenericComponentType())};
        } else {
            Class cl = (Class) c;
            if (cl.isArray()) {
                clazz = (Class<T>) Array.class;
                typeParameters = new Generic[]{new Generic(cl.getComponentType())};
                parent = null;
                return;
            }

            clazz = cl;
            this.typeParameters = new Generic[0];
            if (!clazz.isEnum() && clazz.getGenericSuperclass() != null)
                parent = new Generic(clazz.getGenericSuperclass(), types);
            else
                parent = null;
        }
    }

    @Override
    public String toString() {
        return clazz.getSimpleName();
    }

    public Generic getGenericType(Field f) {
        Generic g = this;
        while (g != null && g.clazz != f.getDeclaringClass()) {
            g = g.parent;
        }
        if (g != null && f.getGenericType() instanceof TypeVariable) {
            return (Generic) g.types.get(((TypeVariable) f.getGenericType()).getName());
        }
        return null;
    }
}
