/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Alan Harder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.copyartifact;

import hudson.EnvVars;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Job;
import hudson.model.Run;
import java.util.List;

/**
 * Extension point for selecting the build to copy artifacts from.
 * In a subclass override just isSelectable() for a standard loop through completed
 * builds, starting with the most recent.  Otherwise override getBuild() to provide
 * different build selection logic.
 * @author Alan Harder
 */
public abstract class BuildSelector extends AbstractDescribableImpl<BuildSelector> implements ExtensionPoint {

    /**
     * Find a build to copy artifacts from.
     * @param job Source project
     * @param runList If non-null, ordered set of builds to examine for a match
     * @param env Environment for build that is copying artifacts
     * @return Build to use, or null if no appropriate build was found
     */
    public Run<?,?> getBuild(Job<?,?> job, List<Run<?,?>> runList, EnvVars env) {
        // Backward compatibility:
        // If this BuildSelector overrides the old API just call it (even though it'll ignore runList)
        if (Util.isOverridden(BuildSelector.class, getClass(), "getBuild", Job.class, EnvVars.class))
            return getBuild(job, env);

        if (runList != null) {
            for (Run<?,?> run : runList)
                if (isSelectable(run, env))
                    return run;
        } else {
            for (Run<?,?> run = job.getLastCompletedBuild(); run != null; run = run.getPreviousCompletedBuild())
                if (isSelectable(run, env))
                    return run;
        }
        return null;
    }

    /**
     * Older version of API.
     */
    @Deprecated
    public Run<?,?> getBuild(Job<?,?> job, EnvVars env) {
        return getBuild(job, null, env);
    }

    /**
     * Should this build be selected?  Override just this method to use a standard
     * loop through completed builds, starting with the most recent.
     * @param run Build to check
     * @param env Environment for build that is copying artifacts
     * @return True to select this build
     */
    protected boolean isSelectable(Run<?,?> run, EnvVars env) {
        return false;
    }
}
