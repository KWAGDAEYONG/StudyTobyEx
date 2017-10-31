package learningtest;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

import java.lang.reflect.Proxy;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HelloTest {

    @Test
    public void simpleProxy(){
        /*Hello hello = new HelloTarget();
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
        assertThat(DynamicproxyHello.sayThankYou("dy"),is("THANK YOU DY"));*/
    }

    @Test
    public void proxyFactoryBean(){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());
        pfBean.addAdvice(new UppercaseAdvice());

        Hello proxiedHello = (Hello) pfBean.getObject();

        assertThat(proxiedHello.sayHello("dy"), is("HELLO DY"));
        assertThat(proxiedHello.sayThankYou("dy"), is("THANK YOU DY"));
        assertThat(proxiedHello.sayHi("dy"),is("HI DY"));
    }

    static class UppercaseAdvice implements MethodInterceptor{
        public Object invoke(MethodInvocation invocation) throws Throwable{
            String ret = (String)invocation.proceed();
            return ret.toUpperCase();
        }
    }

    @Test
    public void pointcutAdvisor(){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());

        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("sayH*");

        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

        Hello proxiedHello = (Hello) pfBean.getObject();

        assertThat(proxiedHello.sayHello("dy"),is("HELLO DY"));
        assertThat(proxiedHello.sayHi("dy"),is("HI DY"));
        assertThat(proxiedHello.sayThankYou("dy"),is("Thank You dy"));
    }

    static interface Hello{
        String sayHello(String name);
        String sayHi(String name);
        String sayThankYou(String name);
    }

    static class HelloTarget implements Hello{
        @Override
        public String sayHello(String name) {
            return "Hello "+name;
        }

        @Override
        public String sayHi(String name) {
            return "Hi "+name;
        }

        @Override
        public String sayThankYou(String name) {
            return "Thank You "+name;
        }
    }
}
