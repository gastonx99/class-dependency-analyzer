package se.dandel.tools.classdepanalyzer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class DebugVisitorInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            Method method = invocation.getMethod();
            StringBuilder builder = new StringBuilder(
                    invocation.getMethod().getDeclaringClass().getSimpleName() + "." + method.getName());
            Object[] arguments = invocation.getArguments();
            Parameter[] parameters = invocation.getMethod().getParameters();
            for (int i1 = 0; i1 < parameters.length; i1++) {

                builder.append(", " + parameters[i1].getName() + "=" + arguments[i1]);

            }
            System.out.println(builder.toString());
            Object result = invocation.proceed();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}