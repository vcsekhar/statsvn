package net.sf.statsvn.util;

public interface ISvnVersionProcessor {

    /**
     * Verifies that the current revision of SVN is SVN_MINIMUM_VERSION
     * 
     * @throws SvnVersionMismatchException
     *             if SVN executable not found or version less than
     *             SVN_MINIMUM_VERSION
     * @return the version string
     */
    public abstract String checkSvnVersionSufficient() throws SvnVersionMismatchException;

    public abstract boolean checkDiffPerRevPossible(final String version);

}