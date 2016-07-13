package com.wizzardo.tools.reflection;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/21/14
 */
public class Generic<T, F extends Fields, G extends Generic> {
    public final Class<T> clazz;
    public final G parent;
    protected Map<String, G> types;
    protected F fields;
    protected final G[] typeParameters;

    public Generic(Type c) {
        this(c, (Map) null);
    }

    public Generic(Class<T> c, Class... generics) {
        clazz = c;
        parent = null;
        if (generics == null) {
            typeParameters = createArray(0);
            return;
        }
        typeParameters = createArray(generics.length);
        for (int i = 0; i < generics.length; i++) {
            typeParameters[i] = create(generics[i]);
        }

        types = getTypes(c, typeParameters);
    }

    public Generic(Class<T> c, G... generics) {
        clazz = c;
        parent = null;
        if (generics == null)
            typeParameters = createArray(0);
        else
            typeParameters = generics;

        types = getTypes(c, typeParameters);
    }

    protected Generic(Type c, Map<String, G> types) {
        if (c instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) c;
            clazz = (Class) type.getRawType();
            Type[] args = type.getActualTypeArguments();
            TypeVariable<Class<T>>[] variables = clazz.getTypeParameters();

            this.types = new HashMap<String, G>();
            this.typeParameters = createArray(args.length);
            for (int i = 0; i < args.length; i++) {
                this.typeParameters[i] = create(args[i], types);
                this.types.put(variables[i].getName(), this.typeParameters[i]);
            }

            if (clazz.getGenericSuperclass() != null)
                parent = create(clazz.getGenericSuperclass(), this.types);
            else
                parent = null;
        } else if (c instanceof TypeVariable) {
            if (types != null) {
                G g = types.get(((TypeVariable) c).getName());
                clazz = g.clazz;
                parent = (G) g.parent;
                typeParameters = (G[]) g.typeParameters;
            } else {
                clazz = (Class<T>) Object.class;
                parent = null;
                typeParameters = createArray(0);
            }
        } else if (c instanceof GenericArrayType) {
            parent = null;
            clazz = (Class<T>) Array.class;
            typeParameters = createArray(1);
            typeParameters[0] = create(((GenericArrayType) c).getGenericComponentType());
        } else {
            Class cl = (Class) c;
            if (cl.isArray()) {
                clazz = (Class<T>) Array.class;
                typeParameters = createArray(1);
                typeParameters[0] = create(cl.getComponentType());
                parent = null;
                return;
            }

            clazz = cl;
            this.typeParameters = createArray(0);
            if (!clazz.isEnum() && clazz.getGenericSuperclass() != null)
                parent = create(clazz.getGenericSuperclass(), types);
            else
                parent = null;
        }
    }

    private Map<String, G> getTypes(Class<T> c, G[] generics) {
        TypeVariable<Class<T>>[] variables = clazz.getTypeParameters();
        if (variables.length == 0 || generics.length == 0)
            return null;

        Map<String, G> types = new HashMap<String, G>();
        for (int i = 0; i < variables.length && i < generics.length; i++) {
            types.put(variables[i].getName(), generics[i]);
        }
        return types;
    }

    public int typesCount() {
        return typeParameters.length;
    }

    public G type(int i) {
        return typeParameters[i];
    }

    @Override
    public String toString() {
        return clazz.getSimpleName();
    }

    public G getGenericType(Field f) {
        Generic g = this;
        while (g != null && g.clazz != f.getDeclaringClass()) {
            g = g.parent;
        }
        if (g != null && f.getGenericType() instanceof TypeVariable) {
            return (G) g.types.get(((TypeVariable) f.getGenericType()).getName());
        }
        return null;
    }

    public F getFields() {
        if (fields != null)
            return fields;

        fields = (F) new Fields(clazz);
        return fields;
    }

    protected G[] createArray(int size) {
        return (G[]) new Generic[size];
    }

    protected G create(Type c) {
        return (G) new Generic(c);
    }

    protected G create(Class<T> c, Class... generics) {
        return (G) new Generic(c, generics);
    }

    protected G create(Type c, Map<String, G> types) {
        return (G) new Generic(c, types);
    }

    public G parent() {
        return parent;
    }
}
