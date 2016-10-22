package jetbrains.buildServer.revisions;

import jetbrains.buildServer.serverSide.BuildRevision;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevisionsParametersProvider extends AbstractBuildParametersProvider {
    private static final Logger LOG = Logger.getLogger(RevisionsParametersProvider.class);
    public static final String PREFIX = "revisions";

    @NotNull
    @Override
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        List<BuildRevision> revisions = build.getRevisions();
        if (revisions == null) return Collections.emptyMap();

        Map<String, String> result = new HashMap<>();
        revisions.forEach(r -> processRevision(r, result, true));
        if (!revisions.isEmpty()) {
            processRevision(revisions.get(0), result, false);
        }
        return result;
    }

    private void processRevision(BuildRevision revision, Map<String, String> result, boolean includeId) {
        String prefix = PREFIX + "." + (includeId ? revision.getRoot().getName() + "." : "");
        result.put(prefix + "revision", revision.getRevision());
        result.put(prefix + "short", isGitRepo(revision.getRoot()) ? revision.getRevision().substring(0, 7) : "N/A");
    }

    private boolean isGitRepo(VcsRootInstance root) {
        return root != null && "jetbrains.git".equalsIgnoreCase(root.getVcsName());
    }
}
