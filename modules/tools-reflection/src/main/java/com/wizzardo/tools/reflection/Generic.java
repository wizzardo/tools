package com.wizzardo.tools.reflection;

import java.lang.reflect.*;
import java.util.*;

import static com.wizzardo.tools.reflection.Misc.*;

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
    protected final Generic[] interfaces;

    public static <T> Generic<T, Fields, Generic> of(Class<T> clazz) {
        return new Generic<T, Fields, Generic>(clazz);
    }

    public Generic(Type c) {
        this(c, (Map) null);
    }

    public Generic(Class<T> c, Class... generics) {
        clazz = c;
        parent = null;
        if (generics == null) {
            typeParameters = createArray(0);
        } else {
            typeParameters = createArray(generics.length);
            for (int i = 0; i < generics.length; i++) {
                typeParameters[i] = create(generics[i]);
            }
        }

        types = getTypes(c, typeParameters);

        Type[] interfaces = clazz.getGenericInterfaces();
        this.interfaces = createInterfaces(interfaces);
        initInterfaces(this.interfaces, interfaces, types, new HashMap<Type, Generic<T, F, G>>());
    }

    public Generic(Class<T> c, G... generics) {
        clazz = c;
        parent = null;
        if (generics == null)
            typeParameters = createArray(0);
        else
            typeParameters = generics;

        types = getTypes(c, typeParameters);

        Type[] interfaces = clazz.getGenericInterfaces();
        this.interfaces = createInterfaces(interfaces);
        initInterfaces(this.interfaces, interfaces, types, new HashMap<Type, Generic<T, F, G>>());
    }

    protected Generic(Type c, Map<String, G> types) {
        this(c, types, new HashMap<Type, Generic<T, F, G>>());
    }

    protected Generic(Type c, Map<String, G> types, Map<Type, Generic<T, F, G>> cyclicDependencies) {
        if (c instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) c;
            clazz = (Class) type.getRawType();
            Type[] args = type.getActualTypeArguments();
            TypeVariable<Class<T>>[] variables = clazz.getTypeParameters();

            this.types = new HashMap<String, G>();
            this.typeParameters = createArray(args.length);
            for (int i = 0; i < args.length; i++) {
                this.typeParameters[i] = create(args[i], types, cyclicDependencies);
                this.types.put(variables[i].getName(), this.typeParameters[i]);
            }

            if (clazz.getGenericSuperclass() != null)
                parent = create(clazz.getGenericSuperclass(), this.types, cyclicDependencies);
            else
                parent = null;

            types = this.types;
        } else if (c instanceof TypeVariable) {
            if (types != null) {
                TypeVariable variable = (TypeVariable) c;
                G g = types.get(variable.getName());
                if (g == null) {
                    Type type = variable.getBounds()[0];
                    g = create(type, types, cyclicDependencies);
                }

                clazz = g.clazz;
                parent = (G) g.parent;
                typeParameters = (G[]) g.typeParameters;
                interfaces = g.interfaces;
                this.types = g.types;
                return;
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
        } else if (c instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) c;
            Type type = wildcardType.getLowerBounds().length > 0 ? wildcardType.getLowerBounds()[0] : wildcardType.getUpperBounds()[0];
            Generic<T, F, G> g = create(type);
            clazz = g.clazz;
            typeParameters = g.typeParameters;
            parent = g.parent;
            interfaces = g.interfaces;
            return;
        } else {
            Class cl = (Class) c;
            if (cl.isArray()) {
                clazz = (Class<T>) Array.class;
                typeParameters = createArray(1);
                typeParameters[0] = create(cl.getComponentType());
                parent = null;
                interfaces = new Generic[0];
                return;
            }
            Generic<T, F, G> cd = cyclicDependencies.get(cl);
            if (cd != null) {
                clazz = cd.clazz;
                parent = cd.parent;
                interfaces = cd.interfaces;
                typeParameters = cd.typeParameters;
                return;
            }

            clazz = cl;
            this.typeParameters = createArray(0);
            if (!clazz.isEnum() && clazz.getGenericSuperclass() != null)
                parent = create(clazz.getGenericSuperclass(), types, cyclicDependencies);
            else
                parent = null;

            cyclicDependencies.put(cl, this);
        }

        Type[] interfaces = clazz.getGenericInterfaces();
        this.interfaces = createInterfaces(interfaces);
        initInterfaces(this.interfaces, interfaces, types, cyclicDependencies);
    }

    protected Generic[] createInterfaces(Type[] interfaces) {
        return new Generic[interfaces.length];
    }

    protected void initInterfaces(Generic[] result, Type[] interfaces, Map<String, G> types, Map<Type, Generic<T, F, G>> cyclicDependencies) {
        for (int i = 0; i < interfaces.length; i++) {
            result[i] = create(interfaces[i], types, cyclicDependencies);
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

    public int getTypesCount() {
        return typesCount();
    }

    public G type(int i) {
        return typeParameters[i];
    }

    public G getType(int i) {
        return type(i);
    }

    public G type(String name) {
        return types.get(name);
    }

    public G getType(String name) {
        return type(name);
    }

    public Map<String, G> types() {
        if (types == null)
            return Collections.emptyMap();

        return Collections.unmodifiableMap(types);
    }

    public Map<String, G> getTypes() {
        return types();
    }

    public int getInterfacesCount() {
        return interfaces.length;
    }

    public Generic getInterface(int i) {
        return interfaces[i];
    }

    @Override
    public String toString() {
        if (typesCount() == 0)
            return clazz.getSimpleName();

        return join(new StringBuilder(32)
                        .append(clazz.getSimpleName())
                        .append('<'),
                typeParameters, ",")
                .append('>')
                .toString();
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

        fields = (F) new Fields(this);
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

    protected G create(Type c, Map<String, G> types, Map<Type, Generic<T, F, G>> cyclicDependencies) {
        return (G) new Generic(c, types, cyclicDependencies);
    }

    public G parent() {
        return parent;
    }

    public List<GenericMethod> methods() {
        List<GenericMethod> methods = new ArrayList<GenericMethod>();

        fillMethods(methods, this);

        return methods;
    }

    protected void fillMethods(List<GenericMethod> methods, Generic generic) {
        Method[] declaredMethods = generic.clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            Generic returnType = new Generic(method.getGenericReturnType(), generic.types);
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            List<Generic> args = new ArrayList<Generic>(genericParameterTypes.length);
            for (Type genericParameterType : genericParameterTypes) {
                args.add(new Generic(genericParameterType, generic.types));
            }
            methods.add(new GenericMethod(method, returnType, Collections.unmodifiableList(args)));
        }

        for (Generic g : generic.interfaces) {
            fillMethods(methods, g);
        }

        if (generic.parent != null)
            fillMethods(methods, generic.parent);
    }
}
