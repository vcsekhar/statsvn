package net.sf.statsvn.util;

public interface ISvnProcessor {
    public abstract ISvnDiffProcessor getDiffProcessor();
    public abstract ISvnInfoProcessor getInfoProcessor();
    public abstract ISvnPropgetProcessor getPropgetProcessor();
    public abstract ISvnVersionProcessor getVersionProcessor();
}