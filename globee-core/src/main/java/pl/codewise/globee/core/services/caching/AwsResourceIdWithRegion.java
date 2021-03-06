package pl.codewise.globee.core.services.caching;

import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import pl.codewise.globee.core.utils.GlobeeStringUtils;

@Data
public abstract class AwsResourceIdWithRegion {

    private final String id;
    private final String region;
    private final boolean toBeDeleted;

    protected AwsResourceIdWithRegion(String id, String region, boolean toBeDeleted) {
        this.id = id;
        this.region = region;
        this.toBeDeleted = toBeDeleted;
    }

    public abstract void visit(ResourceVisitor visitor);

    @VisibleForTesting
    public static Instance instance(String id, String region, boolean toBeDeleted) {
        return new Instance(GlobeeStringUtils.removeQuotationMarks(id), GlobeeStringUtils.removeQuotationMarks(region),
                toBeDeleted);
    }

    static LaunchConfiguration launchConfiguration(String id, String region, boolean toBeDeleted) {
        return new LaunchConfiguration(
                GlobeeStringUtils.removeQuotationMarks(id), GlobeeStringUtils.removeQuotationMarks(region),
                toBeDeleted);
    }

    static AutoScalingGroup autoScalingGroup(String id, String region, boolean toBeDeleted) {
        return new AutoScalingGroup(GlobeeStringUtils.removeQuotationMarks(id),
                GlobeeStringUtils.removeQuotationMarks(region), toBeDeleted);
    }

    public static class Instance extends AwsResourceIdWithRegion {

        private Instance(String id, String region, boolean toBeDeleted) {
            super(id, region, toBeDeleted);
        }

        @Override
        public void visit(ResourceVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class LaunchConfiguration extends AwsResourceIdWithRegion {

        private LaunchConfiguration(String id, String region, boolean toBeDeleted) {
            super(id, region, toBeDeleted);
        }

        @Override
        public void visit(ResourceVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class AutoScalingGroup extends AwsResourceIdWithRegion {

        private AutoScalingGroup(String id, String region, boolean toBeDeleted) {
            super(id, region, toBeDeleted);
        }

        @Override
        public void visit(ResourceVisitor visitor) {
            visitor.visit(this);
        }
    }
}
