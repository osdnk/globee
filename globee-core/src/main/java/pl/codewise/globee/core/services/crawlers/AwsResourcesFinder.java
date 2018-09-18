package pl.codewise.globee.core.services.crawlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class AwsResourcesFinder {

    private final Set<Object> current = Sets.newHashSet();
    private final Set<Object> visited = Sets.newHashSet();

    public List<String> getFieldsWhichValuesContainSearchedPhrase(Object object, String searchedPhrase) {
        List<String> result = Lists.newArrayList();
        try {
            containsFieldWithGivenPhraseImpl(object, searchedPhrase, result);
        } finally {
            visited.clear();
            current.clear();
        }
        return result;
    }

    private void containsFieldWithGivenPhraseImpl(Object object, String searchedPhrase,
            List<String> fieldsWithMatchedPhrase) {
        if (object == null || visited.contains(object)) {
            return;
        }
        if (object instanceof Collection) {
            Collection collection = (Collection) object;
            for (Object element : collection) {
                handleValueObtainedFromConcreteFieldOfTheObject(searchedPhrase, element, fieldsWithMatchedPhrase);
            }
        } else {
            Class<?> superclass = object.getClass().getSuperclass();
            if (superclass != Object.class) {
                for (Field field : superclass.getDeclaredFields()) {
                    searchInConcreteField(object, searchedPhrase, fieldsWithMatchedPhrase, field);
                }
            }
            for (Field field : object.getClass().getDeclaredFields()) {
                searchInConcreteField(object, searchedPhrase, fieldsWithMatchedPhrase, field);
            }
        }
        visited.add(object);
    }

    private void searchInConcreteField(Object object, String searchedPhrase, List<String> fieldsWithMatchedPhrase,
            Field field) {
        field.setAccessible(true);
        if (field.getType().isPrimitive() || field.getType().equals(String.class) ||
                field.getType().equals(Boolean.class) ||
                field.getType().equals(Date.class) || field.getType().equals(LocalDateTime.class) ||
                field.getType().equals(Integer.class) || field.getType().equals(Map.class)) {
            String value = String.valueOf(ReflectionUtils.getField(field, object));
            if (value != null && value.toLowerCase().contains(searchedPhrase.toLowerCase())) {
                fieldsWithMatchedPhrase.add(field.getName());
            }
        } else {
            Object value = ReflectionUtils.getField(field, object);
            handleValueObtainedFromConcreteFieldOfTheObject(searchedPhrase, value, fieldsWithMatchedPhrase);
        }
    }

    private void handleValueObtainedFromConcreteFieldOfTheObject(String searchedPhrase, Object value,
            List<String> fieldsWithMatchedPhrase) {
        if (!current.contains(value)) {
            current.add(value);
            try {
                containsFieldWithGivenPhraseImpl(value, searchedPhrase, fieldsWithMatchedPhrase);
            } finally {
                current.remove(value);
            }
        }
    }
}
