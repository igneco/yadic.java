package com.googlecode.yadic.activators;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.ContainerException;
import com.googlecode.yadic.Resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;

public class OptionActivator implements Callable1<Type, Option> {
    private final Resolver resolver;

    public OptionActivator(final Resolver resolver) {
        this.resolver = resolver;
    }

    public Option call(Type type) throws Exception {
        try {
            return option(resolver.resolve(((ParameterizedType) type).getActualTypeArguments()[0]));
        } catch (ContainerException e) {
            return none();
        }
    }
}