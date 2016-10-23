package jetbrains.buildServer.revisions

import jetbrains.buildServer.serverSide.BuildRevision
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.vcs.VcsRootInstance
import spock.lang.Specification


class RevisionsParametersProviderTest extends Specification {
    RevisionsParametersProvider provider = new RevisionsParametersProvider()

    BuildRevision buildRevision(String vcsName, String name, String revision) {
        return Mock(BuildRevision) {
            getRevision() >> revision
            getRevisionDisplayName() >> revision
            getRoot() >> buildRootInstance(vcsName, name)
        }
    }

    VcsRootInstance buildRootInstance(String vcsName, String name) {
        return Mock(VcsRootInstance) {
            getName() >> name
            getVcsName() >> vcsName
        }
    }

    def "should provide revision information"() {
        given:
        SBuild build = Mock(SBuild) {
            getRevisions() >> [buildRevision('jetbrains.git', 'myid', '139eb35414fdbcc1cfb085bca1bf1560f8150dc4')]
        }
        when:
        Map<String, String> result = provider.getParameters(build, false);
        then:
        result.size() == 4
        result['build.revisions.revision'] == '139eb35414fdbcc1cfb085bca1bf1560f8150dc4'
        result['build.revisions.short'] == '139eb35'
        result['build.revisions.myid.revision'] == '139eb35414fdbcc1cfb085bca1bf1560f8150dc4'
        result['build.revisions.myid.short'] == '139eb35'
    }

    def "should provide revision information when more revisions"() {
        given:
        SBuild build = Mock(SBuild) {
            getRevisions() >> [buildRevision('jetbrains.git', 'myid', '139eb35414fdbcc1cfb085bca1bf1560f8150dc4'),
                               buildRevision('jetbrains.git', 'myid2', '11139eb35414fdbcc1cfb085bca1bf1560f8150d')]
        }
        when:
        Map<String, String> result = provider.getParameters(build, false);
        then:
        result.size() == 6
        result['build.revisions.revision'] == '139eb35414fdbcc1cfb085bca1bf1560f8150dc4'
        result['build.revisions.short'] == '139eb35'
        result['build.revisions.myid.revision'] == '139eb35414fdbcc1cfb085bca1bf1560f8150dc4'
        result['build.revisions.myid.short'] == '139eb35'
        result['build.revisions.myid2.revision'] == '11139eb35414fdbcc1cfb085bca1bf1560f8150d'
        result['build.revisions.myid2.short'] == '11139eb'
    }

    def "should provide short revision only for git"() {
        given:
        SBuild build = Mock(SBuild) {
            getRevisions() >> [buildRevision('jetbrains.git', 'myid', '139eb35414fdbcc1cfb085bca1bf1560f8150dc4'),
                               buildRevision('jetbrains.svn', 'myid2', '1234')]
        }
        when:
        Map<String, String> result = provider.getParameters(build, false);
        then:
        result['build.revisions.myid.revision'] == '139eb35414fdbcc1cfb085bca1bf1560f8150dc4'
        result['build.revisions.myid.short'] == '139eb35'
        result['build.revisions.myid2.revision'] == '1234'
        result['build.revisions.myid2.short'] == 'N/A'
    }

    def "should handle short revision when run in emulation mode"() {
        given:
        SBuild build = Mock(SBuild) {
            getBuildType() >> Mock(SBuildType) {
                getVcsRootInstances() >> [buildRootInstance('jetbrains.git', 'myid')]
            }
        }
        when:
        Map<String, String> result = provider.getParameters(build, true);
        then:
        result.size() == 4
        result['build.revisions.revision'] == '???'
        result['build.revisions.short'] == '???'
        result['build.revisions.myid.revision'] == '???'
        result['build.revisions.myid.short'] == '???'
    }

}
