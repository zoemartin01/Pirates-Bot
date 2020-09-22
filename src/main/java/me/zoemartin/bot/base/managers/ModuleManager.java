package me.zoemartin.bot.base.managers;

import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ModuleManager {
    private static final int SLEEP_DURATION_MS = 500;

    @SuppressWarnings("unchecked")
    public static void init() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                                                      .setUrls(ClasspathHelper.forPackage("me.zoemartin.bot.modules"))
                                                      .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
                                                      .setExecutorService(Executors.newFixedThreadPool(4)));


        Set<Class<?>> modules = reflections.getTypesAnnotatedWith(LoadModule.class);
        modules.removeIf(aClass -> !Set.of(aClass.getInterfaces()).contains(Module.class));
        modules.removeIf(aClass -> {
            try {
                aClass.getMethod("init");
                return false;
            } catch (NoSuchMethodException e) {
                return true;
            }
        });

        Set<Class<? extends Module>> loaded = Collections.newSetFromMap(new ConcurrentHashMap<>());

        modules.parallelStream().forEach(module -> {
            try {
                Set<Class<? extends Module>> loadBefore = Set.of(module.getAnnotation(LoadModule.class).loadAfter());

                while (!loadBefore.isEmpty() && !loaded.containsAll(loadBefore)) {
                    System.out.println("LOADING HALTED ON CLASS " + module.getCanonicalName());
                    Thread.sleep(SLEEP_DURATION_MS);
                }

                module.getMethod("init").invoke(Stream.of(module.getConstructors()).findAny()
                                                    .orElseThrow(RuntimeException::new).newInstance());

                loaded.add((Class<? extends Module>) module);

            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("Loaded Module '%s'\n", module.getName());

        });
    }
}
