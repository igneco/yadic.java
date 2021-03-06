package com.googlecode.yadic.closeable;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.yadic.BaseTypeMap;
import com.googlecode.yadic.Resolver;
import com.googlecode.yadic.TypeMap;
import com.googlecode.yadic.resolvers.Resolvers;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.reflection.Types.classOption;

public class CloseableTypeMap extends BaseTypeMap implements CloseableMap<CloseableTypeMap> {
    private final Map<Type, Closeable> mightClose = new ConcurrentHashMap<Type, Closeable>();
    private final Map<Type, Closeable> mustClose = new ConcurrentHashMap<Type, Closeable>();

    public CloseableTypeMap(Resolver<?> parent) {
        super(parent);
    }

    public void close() throws IOException {
        sequence(mustClose.values()).each(Closeables.safeClose());
    }

    @Override
    public <T> Resolver<T> remove(Type type) {
        removeCloseable(type);
        return super.remove(type);
    }

    @Override
    public TypeMap addType(Type type, Class<? extends Resolver> resolverClass) {
        super.addType(type, resolverClass);
        if (isCloseable(type) && isCloseable(resolverClass)) {
            removeCloseable(type);
        }
        return this;
    }

    @Override
    public TypeMap addType(final Type type, final Type concrete) {
        super.addType(type, concrete);
        if (isCloseable(concrete)) {
            final Resolver<Object> resolver = getResolver(type);
            addCloseable(type, () -> {
                ((Closeable) Resolvers.resolve(resolver, type)).close();
            });
        }
        return this;
    }

    @Override
    public TypeMap addType(Type type, Resolver<?> resolver) {
        if (isCloseable(type) && !(resolver instanceof Closeable)) {
            addCloseable(type, type);
        }

        if (resolver instanceof Closeable) {
            addCloseable(type, (Closeable) resolver);
        }

        return super.addType(type, resolver);
    }

    @Override
    public Object resolve(final Type type) throws Exception {
        Object result = super.resolve(type);
        if (shouldClose(type)) {
            mustClose.put(type, mightClose.get(type));
        }
        return result;
    }

    @Override
    public <T> T create(Type type) {
        T result = super.<T>create(type);
        if (shouldClose(type)) {
            mustClose.put(type, mightClose.get(type));
        }
        return result;
    }

    private Closeable closeActivator(final Type activator) {
        return () -> {
            try {
                Object instance = resolve(activator);
                ((Closeable) instance).close();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }

        };
    }

    private boolean shouldClose(Type type) {
        return mightClose.containsKey(type);
    }

    public static boolean isCloseable(Type aClass) {
        return classOption(aClass).exists(other -> Closeable.class.isAssignableFrom(other));
    }

    public CloseableTypeMap removeCloseable(Type type) {
        mightClose.remove(type);
        mustClose.remove(type);
        return this;
    }

    public CloseableTypeMap addCloseable(final Type aClass, final Closeable activator) {
        removeCloseable(aClass);
        mightClose.put(aClass, activator);
        return this;
    }

    private CloseableTypeMap addCloseable(final Type aClass, final Type activator) {
        return addCloseable(aClass, closeActivator(activator));
    }
}