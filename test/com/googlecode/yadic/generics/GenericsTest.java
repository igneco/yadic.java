package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.activators.OptionActivator;
import com.googlecode.yadic.examples.*;
import org.junit.Test;

import static com.googlecode.yadic.generics.Types.parameterizedType;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GenericsTest {
    @Test
    public void containerShouldSupportGenericClassUsingTypeFor() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.add(new TypeFor<GenericType<Integer>>(){{}}.get(), new TypeFor<GenericType<Integer>>(){{}}.get());
        container.add(UsesGenericType.class);
        UsesGenericType genericType = container.get(UsesGenericType.class);
        assertThat(genericType.instance().instance(), is(1));
    }

    @Test
    public void containerShouldSupportGenericClass() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.add(parameterizedType(GenericType.class, Integer.class), parameterizedType(GenericType.class, Integer.class));
        container.add(UsesGenericType.class);
        UsesGenericType genericType = container.get(UsesGenericType.class);
        assertThat(genericType.instance().instance(), is(1));
    }

    @Test
    public void containerShouldSupportGenericInterfaceAndClass() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.add(parameterizedType(Instance.class, Integer.class), parameterizedType(GenericType.class, Integer.class));
        container.add(UsesGenericType.class);
        UsesGenericType genericType = container.get(UsesGenericType.class);
        assertThat(genericType.instance().instance(), is(1));
    }

    @Test
    public void containerShouldSupportWildcards() throws Exception {
        Container container = new SimpleContainer();
        container.add(new TypeFor<Option<?>>(){{}}.get(), new OptionActivator(container));
        container.add(Node.class, RootNode.class);
        container.add(FlexibleNode.class);
        assertThat(container.get(FlexibleNode.class).parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void containerShouldSupportSomeOption() throws Exception {
        Container container = new SimpleContainer();
        container.add(new TypeFor<Option<Node>>(){{}}.get(), new OptionActivator(container));
        container.add(Node.class, RootNode.class);
        container.add(FlexibleNode.class);
        assertThat(container.get(FlexibleNode.class).parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void containerShouldSupportNoneOption() throws Exception {
        Container container = new SimpleContainer();
        container.add(new TypeFor<Option<Node>>(){{}}.get(), new OptionActivator(container));
        container.add(FlexibleNode.class);
        Option<Node> none = Option.none(Node.class);
        assertThat(container.get(FlexibleNode.class).optionalParent(), is(none));
    }
}