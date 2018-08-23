package pl.codewise.globee.services;

import pl.codewise.globee.services.crawlers.AwsResourcesFinder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AwsResourcesFinderTest {

    private final FakeAwsInstance fakeAwsInstance = new FakeAwsInstance("2018");
    private AwsResourcesFinder awsResourcesFinder = new AwsResourcesFinder();

    @Test
    public void shouldFindSubstringInAwsInstanceStringFieldValue() {
        assertThat(awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "val")).isNotEmpty();
    }

    @Test
    public void shouldFindSubstringInNestedObjectStringFieldValue() {
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "yetAnotherVal")).isNotEmpty();
    }

    @Test
    public void shouldFindSubstringInDoubleNestedObjectStringFieldValue() {
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "twice nested val")).isNotEmpty();
    }

    @Test
    public void shouldFindSubstringInOneOfObjectsFromList() {
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "FirstValue")).isNotEmpty();
    }

    @Test
    public void shouldFindSubstringInFieldOfDateType() {
        assertThat(awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "1970")).isNotEmpty();
    }

    @Test
    public void shouldFindSubstringInFieldOfIntegerType() {
        assertThat(awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "10")).isNotEmpty();
    }

    @Test
    public void shouldNotThrowAnExceptionEvenWhenThereAreNoFieldsInSomeClass() {
        try {
            awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "whatever");
        } catch (Exception e) {
            assertThat(e).isNull();
        }
    }

    @Test
    public void shouldReturnFalseAfterPassingNullObject() {
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(null, "trickyOne")).isEmpty();
    }

    @Test
    public void shouldDetectCircularReferenceAndReturnFalse() {
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "non existing phrase")).isEmpty();
    }

    @Test
    public void shouldFindSubstringIfExistsWhileCircularReferenceIsPresent() {
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "the end")).isNotEmpty();
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "another")).isNotEmpty();
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "twice nested")).isNotEmpty();
        assertThat(awsResourcesFinder
                .getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "yetAnother")).isNotEmpty();
    }

    @Test
    public void shouldFindSubstringInPrimitiveValues() {
        assertThat(awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "tru")).isNotEmpty();
        assertThat(awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "99")).isNotEmpty();
        assertThat(awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "x")).isNotEmpty();
    }

    @Test
    public void shouldFindSubstringInSuperclassField() {
        assertThat(awsResourcesFinder.getFieldsWhichValuesContainSearchedPhrase(fakeAwsInstance, "2018")).isNotEmpty();
    }

    @Getter
    @AllArgsConstructor
    private static class FakeAwsResource {

        private final String id;
    }

    @Getter
    private static class FakeAwsInstance extends FakeAwsResource {

        private final NestedObject nestedObject = new NestedObject();
        private final List<Tag> someTags = Arrays.asList(Tag.builder().key("firstKey").value("andFirstValue").build(),
                Tag.builder().key("secondKey").value("andSecondValue").build());
        private final String field = "Some string value";
        private final DateObject objectWithDate = new DateObject();
        private final IntegerObject objectWithInteger = new IntegerObject();
        private final ClassWithNoFields classWithNoFields = new ClassWithNoFields();
        private final CircularReference circularReference = CircularReference.builder().fakeAwsInstance(this).build();
        private final Primitives primitives = new Primitives();
        private final MapObject mapObject = new MapObject();

        FakeAwsInstance(String id) {
            super(id);
        }
    }

    @Getter
    private static class NestedObject {

        private final CircularReference circularReference = CircularReference.builder().nestedObject(this).build();
        private final EvenMoreNestedObject evenMoreNestedObject = new EvenMoreNestedObject();
        private final String field = "value";
        private final String anotherField = "anotherValue";
        private final String yetAnotherField = "yetAnotherValue";
    }

    @Getter
    private static class EvenMoreNestedObject {

        private final String someField = "twice nested value";
    }

    @Getter
    @Builder
    private static class Tag {

        private final String key;
        private final String value;
    }

    @Getter
    private static class DateObject {

        private final Date date = new Date(0L);
    }

    @Getter
    private static class IntegerObject {

        private final Integer integer = 100;
    }

    private static class ClassWithNoFields {

    }

    @Getter
    @Builder
    private static class CircularReference {

        private final FakeAwsInstance fakeAwsInstance;
        private final NestedObject nestedObject;
        private final String circularReferenceString = "the end";
    }

    @Getter
    private static class Primitives {

        private final boolean bool = true;
        private final char var = 'x';
        private final int num = 999;
    }

    @Getter
    private static class MapObject {

        private static final Map<String, String> map;

        static {
            map = new HashMap<>();
            map.put("uno", "due");
            map.put("tres", "cuatro");
            map.put("cinq", "six");
        }
    }
}