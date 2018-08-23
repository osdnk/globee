package pl.codewise.globee.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobeeStringUtilsTest {

    @Test
    public void shouldRemoveFirstAndLastCharacter() {
        assertThat(GlobeeStringUtils.removeFirstAndLastCharacter("Globee")).isEqualTo("lobe");
    }

    @Test
    public void testExtractJson() {
        assertThat(GlobeeStringUtils.extractJson("\"{\"\\Message\\\":\"\\Info\\\"}\""))
                .isEqualTo("{\"Message\":\"Info\"}");
    }

    @Test
    public void testRemoveQuotationMarks() {
        assertThat(GlobeeStringUtils.removeQuotationMarks("\"GlobeeStringUtils\"")).isEqualTo("GlobeeStringUtils");
    }

    @Test
    public void testExtractExactClassName() {
        assertThat(GlobeeStringUtils.extractExactClassName(
                "com.codewise.globee.services.caching.AwsResourceIdWithRegion$LaunchConfiguration"))
                .isEqualTo("LaunchConfiguration");
        assertThat(GlobeeStringUtils
                .extractExactClassName("com.codewise.globee.services.caching.AwsResourceIdWithRegion$Instance"))
                .isEqualTo("Instance");
    }
}