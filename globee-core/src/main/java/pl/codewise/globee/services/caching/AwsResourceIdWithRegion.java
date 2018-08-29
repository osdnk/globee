package pl.codewise.globee.services.caching;

import lombok.Data;

import static pl.codewise.globee.utils.GlobeeStringUtils.removeQuotationMarks;

@Data
public abstract class AwsResourceIdWithRegion {

    private final String id;
    private final String region;
    private final boolean toBeDeleted;

    private AwsResourceIdWithRegion(String id, String region, boolean toBeDeleted) {
        this.id = id;
        this.region = region;
        this.toBeDeleted = toBeDeleted;
    }

    public abstract void visit(ResourceVisitor visitor);

    public static Instance instance(String id, String region, boolean toBeDeleted) {
        return new Instance(removeQuotationMarks(id), removeQuotationMarks(region), toBeDeleted);
    }

    public static LaunchConfiguration launchConfiguration(String id, String region, boolean toBeDeleted) {
        return new LaunchConfiguration(removeQuotationMarks(id), removeQuotationMarks(region), toBeDeleted);
    }

    public static AutoScalingGroup autoScalingGroup(String id, String region, boolean toBeDeleted) {
        return new AutoScalingGroup(removeQuotationMarks(id), removeQuotationMarks(region), toBeDeleted);
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
