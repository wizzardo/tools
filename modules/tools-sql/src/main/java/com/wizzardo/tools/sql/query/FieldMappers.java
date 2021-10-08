package com.wizzardo.tools.sql.query;

import com.wizzardo.tools.cache.Cache;
import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.reflection.FieldInfo;
import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.Fields;
import com.wizzardo.tools.reflection.Generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FieldMappers {

    static Cache<Class, Fields> fieldsCache = new Cache<>(-1, aClass -> new Generic<>(aClass).getFields());
    static Cache<Class, RecordMapper> classMappers = new Cache<>(-1, FieldMappers::createFromSqlMapper);
    static Cache<FieldSetterKey, FieldSetter> fieldMappers = new Cache<>(-1);

    public static Field.ToSqlMapper toSqlMapper(Class c, Field field) {
        FieldInfo fieldInfo = findField(c, field);
        FieldReflection reflection = fieldInfo.reflection;
        Class<?> fieldType = fieldInfo.field.getType();
        if (fieldType.isPrimitive()) {
            if (fieldType == long.class)
                return (o, builder) -> builder.setField(reflection.getLong(o));
            else if (fieldType == int.class)
                return (o, builder) -> builder.setField(reflection.getInteger(o));
            else if (fieldType == float.class)
                return (o, builder) -> builder.setField(reflection.getFloat(o));
            else if (fieldType == double.class)
                return (o, builder) -> builder.setField(reflection.getDouble(o));
            else if (fieldType == short.class)
                return (o, builder) -> builder.setField(reflection.getShort(o));
            else if (fieldType == boolean.class)
                return (o, builder) -> builder.setField(reflection.getBoolean(o));
            else if (fieldType == byte.class)
                return (o, builder) -> builder.setField(reflection.getByte(o));
        } else if (fieldType == String.class)
            return (o, builder) -> builder.setField((String) reflection.getObject(o));
        else if (fieldType.isEnum())
            return (o, builder) -> {
                Enum anEnum = (Enum) reflection.getObject(o);
                builder.setField(anEnum == null ? null : anEnum.name());
            };
        else if (fieldType == Date.class)
            return (o, builder) -> builder.setField((Date) reflection.getObject(o));
        else if (fieldType == Timestamp.class)
            return (o, builder) -> builder.setField((Timestamp) reflection.getObject(o));
        else if (fieldType == Long.class)
            return (o, builder) -> builder.setField((Long) reflection.getObject(o));
        else if (fieldType == Integer.class)
            return (o, builder) -> builder.setField((Integer) reflection.getObject(o));
        else if (fieldType == Float.class)
            return (o, builder) -> builder.setField((Float) reflection.getObject(o));
        else if (fieldType == Double.class)
            return (o, builder) -> builder.setField((Double) reflection.getObject(o));
        else if (fieldType == Short.class)
            return (o, builder) -> builder.setField((Short) reflection.getObject(o));
        else if (fieldType == Boolean.class)
            return (o, builder) -> builder.setField((Boolean) reflection.getObject(o));
        else if (fieldType == Byte.class)
            return (o, builder) -> builder.setField((Byte) reflection.getObject(o));
        else if (fieldType.isArray()){
            Class componentType = fieldType.getComponentType();
            if (!componentType.isPrimitive())
                throw new IllegalArgumentException("Only primitive arrays are supported for now");

            if (componentType == byte.class) {
                return (o, builder) -> builder.setField((byte[]) reflection.getObject(o));
            }

            throw new IllegalArgumentException("Only byte arrays are supported for now");
        }

        throw new IllegalArgumentException("Cannot create mapper for " + field + " of type " + fieldType);
    }

    public static <R> RecordMapper<R> fromSqlMapper(Class<R> c) {
        return classMappers.get(c);
    }

    private static <R> RecordMapper<R> createFromSqlMapper(Class<R> c) {
        if (c == String.class) {
            return rs -> {
                try {
                    return (R) rs.getString(1);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        }
        if (c == Date.class) {
            return rs -> {
                try {
                    return (R) rs.getDate(1);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        }
        if (c == Timestamp.class) {
            return rs -> {
                try {
                    return (R) rs.getTimestamp(1);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        }
        if (c.isPrimitive()) {
            if (c == long.class)
                return rs -> {
                    try {
                        return (R) ((Long) rs.getLong(1));
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                };
            if (c == int.class)
                return rs -> {
                    try {
                        return (R) ((Integer) rs.getInt(1));
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                };
            if (c == short.class)
                return rs -> {
                    try {
                        return (R) ((Short) rs.getShort(1));
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                };
            if (c == byte.class)
                return rs -> {
                    try {
                        return (R) ((Byte) rs.getByte(1));
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                };
            if (c == double.class)
                return rs -> {
                    try {
                        return (R) ((Double) rs.getDouble(1));
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                };
            if (c == float.class)
                return rs -> {
                    try {
                        return (R) ((Float) rs.getFloat(1));
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                };
            if (c == boolean.class)
                return rs -> {
                    try {
                        return (R) ((Boolean) rs.getBoolean(1));
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                };
        }
        if (c == Long.class)
            return rs -> {
                try {
                    long value = rs.getLong(1);
                    return (R) (value == 0 && rs.wasNull() ? null : value);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        if (c == Integer.class)
            return rs -> {
                try {
                    int value = rs.getInt(1);
                    return (R) (value == 0 && rs.wasNull() ? null : value);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        if (c == Short.class)
            return rs -> {
                try {
                    short value = rs.getShort(1);
                    return (R) (value == 0 && rs.wasNull() ? null : value);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        if (c == Byte.class)
            return rs -> {
                try {
                    byte value = rs.getByte(1);
                    return (R) (value == 0 && rs.wasNull() ? null : value);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        if (c == Double.class)
            return rs -> {
                try {
                    double value = rs.getDouble(1);
                    return (R) (value == 0 && rs.wasNull() ? null : value);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        if (c == Float.class)
            return rs -> {
                try {
                    float value = rs.getFloat(1);
                    return (R) (value == 0 && rs.wasNull() ? null : value);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };
        if (c == Boolean.class)
            return rs -> {
                try {
                    boolean value = rs.getBoolean(1);
                    return (R) (!value && rs.wasNull() ? null : value);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            };

        Fields<FieldInfo> fields = fieldsCache.get(c);
        List<FieldSetter> setters = StreamSupport.stream(fields.spliterator(), false)
                .map(it -> getSetter(it, Generator.toSqlName(it.field.getName())))
                .collect(Collectors.toList());
        return rs -> {
            R r = Unchecked.call(() -> c.newInstance());
            for (FieldSetter it : setters) {
                try {
                    it.set(r, rs);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            }
            return r;
        };
    }

    static class FieldSetterKey {
        final int i;
        final Class c;
        final Field f;

        FieldSetterKey(int i, Class c, Field f) {
            this.i = i;
            this.c = c;
            this.f = f;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FieldSetterKey that = (FieldSetterKey) o;

            if (i != that.i) return false;
            if (!c.equals(that.c)) return false;
            return f.equals(that.f);
        }

        @Override
        public int hashCode() {
            int result = i;
            result = 31 * result + c.hashCode();
            result = 31 * result + f.hashCode();
            return result;
        }
    }

    public static <R> RecordMapper<R> fromSqlMapper(Class<R> c, List<Field> fields) {
        List<FieldSetter> setters = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            FieldSetterKey key = new FieldSetterKey(i + 1, c, field);
            FieldSetter fieldSetter = fieldMappers.get(key);
            if (fieldSetter == null) {
                FieldInfo fieldInfo = findField(c, field);
                fieldSetter = getSetter(fieldInfo, i + 1);
                fieldMappers.put(key, fieldSetter);
            }
            setters.add(fieldSetter);
        }

        return rs -> {
            R r = Unchecked.call(() -> c.newInstance());
            for (FieldSetter it : setters) {
                try {
                    it.set(r, rs);
                } catch (SQLException e) {
                    throw Unchecked.rethrow(e);
                }
            }
            return r;
        };
    }

    private static <R> FieldInfo findField(Class<R> c, Field field) {
        Fields<FieldInfo<?, ?>> fields = fieldsCache.get(c);
        FieldInfo fieldInfo = fields.get(field.getName());
        if (fieldInfo == null) {
            fieldInfo = fields.get(toCamelCase(field.getName()));
        }
        if (fieldInfo == null)
            fieldInfo = StreamSupport.stream(fields.spliterator(), false)
                    .filter(it -> it.field.getName().equalsIgnoreCase(field.getName()))
                    .findFirst().orElse(null);
        if (fieldInfo == null)
            fieldInfo = StreamSupport.stream(fields.spliterator(), false)
                    .filter(it -> it.field.getName().equalsIgnoreCase(toCamelCase(field.getName())))
                    .findFirst().orElse(null);

        if (fieldInfo == null)
            throw new IllegalArgumentException("Cannot find field " + field.getName() + " in class " + c.getSimpleName());

        return fieldInfo;
    }

    public static FieldSetter getSetter(FieldInfo fieldInfo, String name) {
        FieldReflection reflection = fieldInfo.reflection;
        Class<?> fieldType = fieldInfo.field.getType();

        if (fieldType.isPrimitive()) {
            if (fieldType == long.class)
                return (instance, rs) -> reflection.setLong(instance, rs.getLong(name));
            else if (fieldType == int.class)
                return (instance, rs) -> reflection.setInteger(instance, rs.getInt(name));
            else if (fieldType == float.class)
                return (instance, rs) -> reflection.setFloat(instance, rs.getFloat(name));
            else if (fieldType == double.class)
                return (instance, rs) -> reflection.setDouble(instance, rs.getDouble(name));
            else if (fieldType == short.class)
                return (instance, rs) -> reflection.setShort(instance, rs.getShort(name));
            else if (fieldType == boolean.class)
                return (instance, rs) -> reflection.setBoolean(instance, rs.getBoolean(name));
            else if (fieldType == byte.class)
                return (instance, rs) -> reflection.setByte(instance, rs.getByte(name));
        } else if (fieldType == String.class)
            return (instance, rs) -> reflection.setObject(instance, rs.getString(name));
        else if (fieldType == Date.class)
            return (instance, rs) -> reflection.setObject(instance, rs.getDate(name));
        else if (fieldType == Timestamp.class)
            return (instance, rs) -> reflection.setObject(instance, rs.getTimestamp(name));
        else if (fieldType == Long.class)
            return (instance, rs) -> {
                long value = rs.getLong(name);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Integer.class)
            return (instance, rs) -> {
                int value = rs.getInt(name);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Float.class)
            return (instance, rs) -> {
                float value = rs.getFloat(name);
                reflection.setObject(instance, value == 0f && rs.wasNull() ? null : value);
            };
        else if (fieldType == Double.class)
            return (instance, rs) -> {
                double value = rs.getDouble(name);
                reflection.setObject(instance, value == 0.0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Short.class)
            return (instance, rs) -> {
                short value = rs.getShort(name);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Boolean.class)
            return (instance, rs) -> {
                boolean value = rs.getBoolean(name);
                reflection.setObject(instance, !value && rs.wasNull() ? null : value);
            };
        else if (fieldType == Byte.class)
            return (instance, rs) -> {
                byte value = rs.getByte(name);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType.isArray()) {
            Class<?> componentType = fieldType.getComponentType();
            if (componentType == byte.class)
                return (instance, rs) -> {
                    byte[] value = rs.getBytes(name);
                    reflection.setObject(instance, value);
                };
        }

        throw new IllegalArgumentException("Cannot create mapper for " + fieldInfo.field + " of type " + fieldType);
    }

    public static FieldSetter getSetter(FieldInfo fieldInfo, int column) {
        FieldReflection reflection = fieldInfo.reflection;
        Class<?> fieldType = fieldInfo.field.getType();

        if (fieldType.isPrimitive()) {
            if (fieldType == long.class)
                return (instance, rs) -> reflection.setLong(instance, rs.getLong(column));
            else if (fieldType == int.class)
                return (instance, rs) -> reflection.setInteger(instance, rs.getInt(column));
            else if (fieldType == float.class)
                return (instance, rs) -> reflection.setFloat(instance, rs.getFloat(column));
            else if (fieldType == double.class)
                return (instance, rs) -> reflection.setDouble(instance, rs.getDouble(column));
            else if (fieldType == short.class)
                return (instance, rs) -> reflection.setShort(instance, rs.getShort(column));
            else if (fieldType == boolean.class)
                return (instance, rs) -> reflection.setBoolean(instance, rs.getBoolean(column));
            else if (fieldType == byte.class)
                return (instance, rs) -> reflection.setByte(instance, rs.getByte(column));
        } else if (fieldType == String.class)
            return (instance, rs) -> reflection.setObject(instance, rs.getString(column));
        else if (fieldType.isEnum())
            return (instance, rs) -> {
                String value = rs.getString(column);
                reflection.setObject(instance, value == null ? null : Enum.valueOf((Class<Enum>) fieldType, value));
            };
        else if (fieldType == Date.class)
            return (instance, rs) -> reflection.setObject(instance, rs.getDate(column));
        else if (fieldType == Timestamp.class)
            return (instance, rs) -> reflection.setObject(instance, rs.getTimestamp(column));
        else if (fieldType == Long.class)
            return (instance, rs) -> {
                long value = rs.getLong(column);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Integer.class)
            return (instance, rs) -> {
                int value = rs.getInt(column);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Float.class)
            return (instance, rs) -> {
                float value = rs.getFloat(column);
                reflection.setObject(instance, value == 0f && rs.wasNull() ? null : value);
            };
        else if (fieldType == Double.class)
            return (instance, rs) -> {
                double value = rs.getDouble(column);
                reflection.setObject(instance, value == 0.0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Short.class)
            return (instance, rs) -> {
                short value = rs.getShort(column);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType == Boolean.class)
            return (instance, rs) -> {
                boolean value = rs.getBoolean(column);
                reflection.setObject(instance, !value && rs.wasNull() ? null : value);
            };
        else if (fieldType == Byte.class)
            return (instance, rs) -> {
                byte value = rs.getByte(column);
                reflection.setObject(instance, value == 0 && rs.wasNull() ? null : value);
            };
        else if (fieldType.isArray()) {
            Class<?> componentType = fieldType.getComponentType();
            if (componentType == long.class)
                return (instance, rs) -> {
                    System.out.println();
                    reflection.setObject(instance, rs.getArray(column));
                };
            else if (componentType == byte.class)
                return (instance, rs) -> {
                    reflection.setObject(instance, rs.getBytes(column));
                };
        }

        throw new IllegalArgumentException("Cannot create mapper for " + fieldInfo.field + " of type " + fieldType);
    }

    interface FieldSetter {
        void set(Object instance, ResultSet rs) throws SQLException;
    }

    public interface RecordMapper<E> {
        E map(ResultSet record);
    }


    private static String toCamelCase(String string) {
        StringBuilder result = new StringBuilder();

        for (String word : string.split("_", -1)) {
            if (result.length() > 0 && word.length() > 0) {
                result.append(word.substring(0, 1).toUpperCase());
                result.append(word.substring(1).toLowerCase());
            } else {
                result.append(word);
            }
        }

        return result.toString();
    }
}
