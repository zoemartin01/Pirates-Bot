package me.zoemartin.piratesBot.core.managers;

import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.interfaces.Module;
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
    private static final Collection<Module> modules = Collections.newSetFromMap(new ConcurrentHashMap<>());

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

        modules.forEach(module -> {
            Set<Class<? extends Module>> loadBefore = Set.of(module.getAnnotation(LoadModule.class).loadAfter());

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                if (loadBefore.isEmpty() || loaded.containsAll(loadBefore)) {
                    Module m;
                    try {
                        m = (Module) Stream.of(module.getConstructors()).findAny()
                                                               .orElseThrow(RuntimeException::new).newInstance();
                        m.init();
                        ModuleManager.modules.add(m);
                    } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                        e.printStackTrace();
                    }
                    loaded.add((Class<? extends Module>) module);
                    executor.shutdown();
                }

                System.out.printf("Loaded Module '%s'\n", module.getName());
            }, 0, SLEEP_DURATION_MS, TimeUnit.MILLISECONDS);
        });
    }

    public static void initLate() {
        modules.forEach(Module::initLate);
    }
}
