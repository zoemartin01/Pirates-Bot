package me.zoemartin.bot.base.managers;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
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
            Set<Class<? extends Module>> loadBefore = Set.of(module.getAnnotation(LoadModule.class).loadAfter());

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                if (loadBefore.isEmpty() || loaded.containsAll(loadBefore)) {
                    try {
                        module.getMethod("init").invoke(Stream.of(module.getConstructors()).findAny()
                                                            .orElseThrow(RuntimeException::new).newInstance());
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                        e.printStackTrace();
                    }

                    loaded.add((Class<? extends Module>) module);
                    executor.shutdown();
                }

                System.out.printf("Loaded Module '%s'\n", module.getName());
            }, 0, SLEEP_DURATION_MS, TimeUnit.MILLISECONDS);
        });
    }
}
