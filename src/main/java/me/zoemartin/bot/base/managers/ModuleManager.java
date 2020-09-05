package me.zoemartin.bot.base.managers;

import me.zoemartin.bot.base.LoadModule;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ModuleManager {
    public static void init() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                                                      .setUrls(ClasspathHelper.forPackage(""))
                                                      .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
                                                      .setExecutorService(Executors.newFixedThreadPool(4)));

        Set<Class<?>> modules = reflections.getTypesAnnotatedWith(LoadModule.class);

        modules.forEach(module -> new Thread(() -> {
            try {
                module.getMethod("init").invoke(Stream.of(module.getConstructors()).findAny()
                                                    .orElseThrow(RuntimeException::new).newInstance());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                e.printStackTrace();
            }
            System.out.printf("Loaded Module '%s'\n", module.getName());

        }).start());
    }
}
