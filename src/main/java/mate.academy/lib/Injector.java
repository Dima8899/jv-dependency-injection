package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Map<Class<?>, Class<?>> interfaceImpl =
            Map.of(FileReaderService.class, FileReaderServiceImpl.class, ProductParser.class,
                    ProductParserImpl.class, ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> implClass = getImplclass(interfaceClazz);
        Field[] declaredFields = implClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(implClass);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't initialize field value. " + "Class: " + implClass.getName()
                                    + " Field: " + field.getName(), e);
                }

            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(implClass);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed: missing @Component on class " + clazz.getName());
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of" + clazz.getName(), e);
        }
    }

    private Class getImplclass(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceImpl.get(interfaceClazz) == null) {
                throw new RuntimeException("There is no implementation for" + interfaceClazz);
            }
            return interfaceImpl.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
