package me.zoemartin.piratesBot.core.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseConverter {
    @Converter
    public static class StringListConverter implements AttributeConverter<Collection<String>, String> {
        private static final String SPLIT_CHAR = String.valueOf((char) 7);

        @Override
        public String convertToDatabaseColumn(Collection<String> stringList) {
            return String.join(SPLIT_CHAR, stringList);
        }

        @Override
        public Collection<String> convertToEntityAttribute(String string) {
            Collection<String> collection = Collections.newSetFromMap(new ConcurrentHashMap<>());
            collection.addAll(Arrays.asList(string.split(SPLIT_CHAR)));
            return collection;
        }
    }
}
