package pl.codewise.globee.services.caching;

import lombok.Data;

import static pl.codewise.globee.utils.GlobeeStringUtils.removeQuotationMarks;

@Data
public abstract class AwsResourceIdWithRegion {

    private final String id;
    private final String region;
    private final boolean shouldResourceBeDeleted;

    private AwsResourceIdWithRegion(String id, String region, boolean shouldResourceBeDeleted) {
        this.id = id;
        this.region = region;
        this.shouldResourceBeDeleted = shouldResourceBeDeleted;
    }

    public abstract void visit(ResourceVisitor visitor);

    public static Instance instance(String id, String region, boolean shouldResourceBeDeleted) {
        return new Instance(removeQuotationMarks(id), removeQuotationMarks(region), shouldResourceBeDeleted);
    }

    public static LaunchConfiguration launchConfiguration(String id, String region, boolean shouldResourceBeDeleted) {
        return new LaunchConfiguration(removeQuotationMarks(id), removeQuotationMarks(region), shouldResourceBeDeleted);
    }

    public static AutoScalingGroup autoScalingGroup(String id, String region, boolean shouldResourceBeDeleted) {
        return new AutoScalingGroup(removeQuotationMarks(id), removeQuotationMarks(region), shouldResourceBeDeleted);
    }

    public static class Instance extends AwsResourceIdWithRegion {

        private Instance(String id, String region, boolean shouldResourceBeDeleted) {
            super(id, region, shouldResourceBeDeleted);
        }

        @Override
        public void visit(ResourceVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class LaunchConfiguration extends AwsResourceIdWithRegion {

        private LaunchConfiguration(String id, String region, boolean shouldResourceBeDeleted) {
            super(id, region, shouldResourceBeDeleted);
        }

        @Override
        public void visit(ResourceVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class AutoScalingGroup extends AwsResourceIdWithRegion {

        private AutoScalingGroup(String id, String region, boolean shouldResourceBeDeleted) {
            super(id, region, shouldResourceBeDeleted);
        }

        @Override
        public void visit(ResourceVisitor visitor) {
            visitor.visit(this);
        }
    }
}
