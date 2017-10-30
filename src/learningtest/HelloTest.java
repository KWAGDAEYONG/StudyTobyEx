package learningtest;

import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HelloTest {

    @Test
    public void simpleProxy(){
        Hello hello = new HelloTarget();
        assertThat(hello.sayHello("dy"), is("Hello dy"));
        assertThat(hello.sayHi("dy"), is("Hi dy"));
        assertThat(hello.sayThankYou("dy"), is("Thank You dy"));

        Hello proxyHello = new HelloUppercase(new HelloTarget());

        assertThat(proxyHello.sayHello("dy"), is("HELLO DY"));
        assertThat(proxyHello.sayHi("dy"), is("HI DY"));
        assertThat(proxyHello.sayThankYou("dy"),is("THANK YOU DY"));

        Hello DynamicproxyHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new UppercaseHandler(new HelloTarget())
        );

        assertThat(DynamicproxyHello.sayHello("dy"), is("HELLO DY"));
        assertThat(DynamicproxyHello.sayHi("dy"), is("HI DY"));
        assertThat(DynamicproxyHello.sayThankYou("dy"),is("THANK YOU DY"));


    }
}
