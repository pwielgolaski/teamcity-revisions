package jetbrains.buildServer.revisions;

import jetbrains.buildServer.serverSide.BuildRevision;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.impl.DummyBuildType;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import jetbrains.buildServer.vcs.VcsRootInstance;
import jetbrains.buildServer.vcs.VcsRootInstanceEntry;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RevisionsParametersProvider extends AbstractBuildParametersProvider {
    private static final Logger LOG = Logger.getLogger(RevisionsParametersProvider.class);
    public static final String PREFIX = "build.revisions.";

    @NotNull
    @Override
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        Map<VcsRootInstance, BuildRevision> revisions = getRevisions(build, emulationMode);
        Map<String, String> result = new HashMap<>();
        revisions.forEach((root, revision) -> processRevision(root, revision, result, true));
        if (!revisions.isEmpty()) {
            Map.Entry<VcsRootInstance, BuildRevision> revision = revisions.entrySet().iterator().next();
            processRevision(revision.getKey(), revision.getValue(), result, false);
        }
        LOG.debug(result);
        return result;
    }

    private Map<VcsRootInstance, BuildRevision> getRevisions(@NotNull SBuild build, boolean emulationMode) {
        final Map<VcsRootInstance, BuildRevision> revisions = new LinkedHashMap<>();
        if (emulationMode) {
            SBuildType buildType = build.getBuildType();
            if (buildType == null) return revisions;

            for (VcsRootInstanceEntry rootInstanceEntry : buildType.getVcsRootInstanceEntries()) {
                revisions.put(rootInstanceEntry.getVcsRoot(), null);
            }

            if ((buildType instanceof DummyBuildType)) return revisions;

            for (VcsRootInstance rootInstance : buildType.getVcsRootInstances()) {
                revisions.put(rootInstance, null);
            }
        } else if (build.getRevisions() != null) {
            build.getRevisions().forEach(r -> revisions.put(r.getRoot(), r));
        }
        return revisions;
    }

    private void processRevision(VcsRootInstance root, BuildRevision revision, Map<String, String> result, boolean includeId) {
        String prefix = PREFIX + (includeId ? root.getName() + "." : "");
        String revisionText = revision != null ? revision.getRevision() : "???";
        result.put(prefix + "revision", revisionText);
        result.put(prefix + "short", isGitRepo(root) ? revisionText.substring(0, Math.min(revisionText.length(), 7)) : "N/A");
    }

    private boolean isGitRepo(VcsRootInstance root) {
        return root != null && "jetbrains.git".equalsIgnoreCase(root.getVcsName());
    }
}
