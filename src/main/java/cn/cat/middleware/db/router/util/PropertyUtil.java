package cn.cat.middleware.db.router.util;

import org.springframework.core.SpringVersion;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class PropertyUtil {
    private static final int springBootVersion;

    static {
        String springBootVersionStr = SpringVersion.getVersion();
        assert springBootVersionStr != null;
        if (springBootVersionStr.startsWith("3")) {
            springBootVersion = 3;
        } else if (springBootVersionStr.startsWith("2")) {
            springBootVersion = 2;
        } else {
            springBootVersion = 1;
        }
    }

    public static <T> T handle(final Environment environment, final String prefix, final Class<?> mapClass) {
        return switch (springBootVersion) {
            case 1 -> (T) v1(environment, prefix);
            case 2, 3 -> (T) v2And3(environment, prefix, mapClass);
            default -> throw new RuntimeException("Unsupported Spring Boot version: " + springBootVersion);
        };
    }

    private static Object v1(final Environment environment, final String prefix) {
        try {
            Class<?> resolverClass = Class.forName("org.springframework.boot.bind.PropertySourcesPropertyResolver");
            Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
            Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
            // 反射创建 PropertySourcesPropertyResolver 对象
            Object resolverObject = resolverConstructor.newInstance(environment);
            String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
            return getSubPropertiesMethod.invoke(resolverObject, prefixParam);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                       | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private static Object v2And3(final Environment environment, final String prefix, final Class<?> targetClass) {
        try {
            Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
            Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);

            // 获取 Binder 实例
            Object binderObject = getMethod.invoke(null, environment);
            String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;

            // 调用 bind 方法
            Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);

            // 获取绑定结果并返回
            Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
            return resultGetMethod.invoke(bindResultObject);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException
                       | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
