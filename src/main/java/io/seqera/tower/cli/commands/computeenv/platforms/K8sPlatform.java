package io.seqera.tower.cli.commands.computeenv.platforms;

import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.K8sComputeConfig;
import io.seqera.tower.model.PodCleanupPolicy;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class K8sPlatform extends AbstractPlatform<K8sComputeConfig> {

    @Option(names = {"--server"}, description = "Master server")
    public String server;

    @Option(names = {"--ssl-cert"}, description = "SSL certificate")
    public Path sslCert;

    @Option(names = {"--namespace"}, description = "Namespace")
    public String namespace;

    @Option(names = {"--head-service-account"}, description = "Head service account")
    public String headServiceAccount;

    @Option(names = {"--storage-claim-name"}, description = "Storage claim name")
    public String storageClaimName;

    @Option(names = {"--compute-service-account"}, description = "Compute service account")
    public String computeServiceAccount;

    @Option(names = {"--storage-mount-path"}, description = "Storage mount path")
    public String storageMountPath;

    @Option(names = {"--service-pod-spec"}, description = "Custom service pod specs file")
    public Path servicePodSpec;

    @Option(names = {"--head-pod-spec"}, description = "Custom head pod specs file")
    public Path headPodSpec;

    @Option(names = "--pod-cleanup", description = "Pod cleanup policy (${COMPLETION-CANDIDATES})")
    public PodCleanupPolicy podCleanup;

    public K8sPlatform() {
        super(PlatformEnum.K8S_PLATFORM);
    }

    @Override
    public K8sComputeConfig computeConfig() throws IOException {
        return new K8sComputeConfig()
                .platform(PlatformEnum.K8S_PLATFORM.name())
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .computeServiceAccount(computeServiceAccount)
                .headPodSpec(Files.readString(headPodSpec))
                .headServiceAccount(headServiceAccount)
                .namespace(namespace)
                .podCleanup(podCleanup)
                .server(server)
                .servicePodSpec(Files.readString(servicePodSpec))
                .sslCert(Files.readString(sslCert))
                .storageClaimName(storageClaimName)
                .storageMountPath(storageMountPath);
    }
}
