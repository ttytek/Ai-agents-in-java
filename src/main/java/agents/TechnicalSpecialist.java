package agents;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Scanner;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;

import io.reactivex.rxjava3.core.Flowable;

import java.util.Map;

public class TechnicalSpecialist {

    public static final BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {
        return LlmAgent.builder()
            .name("Technical specialist")
            .description("Technical specialist")
            .model("gemini-2.5-flash")
            .instruction(
                "You are a technical specialist tasked with supporting customers. "
              + "Your answers must be backed by factual information from documentation. "
              + "If the user has a problem, check troubleshooting notes. "
              + "If the user is asking for general tips, check integration tips. "
              + "If the user is asking about some implementation detail, refer to source code. "
              + "If the provided tools don't cover the user's request, say so or ask for clarification. "
              + "You must not guess."
            )
            .tools(
                FunctionTool.create(TechnicalSpecialist.class, "troubleshootingNotes"),
                FunctionTool.create(TechnicalSpecialist.class, "integrationTips"),
                FunctionTool.create(TechnicalSpecialist.class, "sourceCode")
            )
            .build();
    }

    @Schema(description = "Get the contents of troubleshooting notes")
    public static Map<String, String> troubleshootingNotes() {
        return Map.of(
            "content",
                "1. Service Fails to Start\n"
              + "\n"
              + "Symptoms:\n"
              + "\n"
              + "nebula-sync.service stays in an “activating” state\n"
              + "Logs show: Fatal: Cannot bind to port\n"
              + "\n"
              + "Common causes and fixes:\n"
              + "\n"
              + "The default port (4799) may be in use. Run lsof -i :4799, stop the conflicting process, or change the port in config.toml.\n"
              + "The encryption key file may be missing. The file keys/node.key must exist and its permissions should be set to 600.\n"
              + "The configuration file may be corrupted. Run nebula-sync --validate-config to confirm and correct issues.\n"
              + "\n"
              + "2. Nodes Are Not Replicating Files\n"
              + "\n"
              + "Symptoms:\n"
              + "Files never appear on the secondary node\n"
              + "The primary reports a successful upload, but replicas do not update\n"
              + "\n"
              + "Possible causes and fixes:\n"
              + "If device clocks differ by more than 30 seconds, replication is blocked. Sync time using ntpdate pool.ntp.org or enable ntpd/chrony.\n"
              + "The peer node may be unreachable. Test connectivity with nebula-sync ping <node-id>.\n"
              + "Metadata might be inconsistent. Clear it with nebula-sync --reset-meta.\n"
              + "\n"
              + "3. High CPU Usage\n"
              + "\n"
              + "Symptoms:\n"
              + "Replication threads reach 90% or higher CPU usage\n"
              + "Happens primarily during large batch synchronization\n"
              + "\n"
              + "Fixes:\n"
              + "Set compression mode to “adaptive” in the config to reduce CPU load.\n"
              + "Reduce concurrent replication workers by setting replication_workers=2.\n"
              + "Ensure that hardware AES acceleration is available; check with dmesg | grep aes.\n"
              + "\n"
              + "4. Files Seem Corrupted After Sync\n"
              + "\n"
              + "Symptoms:\n"
              + "Downloaded files fail checksum validation\n"
              + "Logs show: Block verification failed\n"
              + "\n"
              + "Causes and fixes:\n"
              + "A node may be running an older hashing scheme. All nodes must run version 2.4 or newer.\n"
              + "The cache directory might be on a failing disk. Run fsck or change cache_dir to an SSD-backed path such as /mnt/storage_ssd.\n"
              + "\n"
              + "5. Web Dashboard Will Not Load\n"
              + "\n"
              + "Symptoms:\n"
              + "Browser displays a blank page\n"
              + "System logs do not show an obvious error\n"
              + "\n"
              + "Fixes:\n"
              + "The dashboard may have been built without frontend assets. Rebuild with make build-ui or reinstall the package.\n"
              + "Remote UI access may be blocked by CORS settings. In the configuration file, enable allow_remote_ui=true.\n"
              + "\n"
              + "6. Initial Sync Is Very Slow\n"
              + "\n"
              + "Checklist:\n"
              + "Make sure delta transfers are enabled (--enable-delta-transfer=true).\n"
              + "Use a wired network during the first full synchronization.\n"
              + "Check disk I/O speed using iostat -xm 1. If the device is above 70% utilization, sync will slow significantly.\n"
              + "\n"
              + "7. Quick Diagnostic Commands\n"
        );
    }

    @Schema(description = "Get integration tips")
    public static Map<String, String> integrationTips() {
        return Map.of(
            "content",
                "The most reliable way to integrate NebulaSync is to use its local API rather than writing directly to the storage directory on disk. "
              + "Even though the encrypted blocks are stored locally, direct file writes will not trigger block assignment, metadata updates, or replication, "
              + "so all new content should be uploaded through the file-upload endpoint or the official CLI. Replication should be treated as eventually "
              + "consistent rather than immediate. If another system depends on a file being present on all nodes, it should poll the replication status "
              + "or wait for a callback.\n"
              + "\n"
              + "NebulaSync can send outbound webhooks when files are added or when replication completes. This is useful when downstream systems must wait "
              + "until data is everywhere before processing it. Systems using NebulaSync should monitor free disk space carefully — the service can accept "
              + "files even when nearly full, so alerts should trigger before storage becomes critically low.\n"
              + "\n"
              + "Large files update more efficiently with delta transfers enabled, reducing bandwidth and speeding up replication. Nodes may be grouped by "
              + "labels such as region or role to restrict replication.\n"
              + "\n"
              + "Health checks should be integrated into orchestration. The service exposes a machine-readable status, and the orchestrator can restart the "
              + "process if health checks fail. On low-power devices, the cache should remove cold data to prevent storage exhaustion.\n"
              + "\n"
              + "Encryption keys should not be stored in configuration files. Use environment variables or a secret-management tool. Before production deployment, "
              + "simulate network failures, add files, reconnect nodes, and verify checksums to ensure replication behaves correctly under real-world conditions.\n"
        );
    }

    @Schema(description = "Get project source code")
    public static Map<String, String> sourceCode() {
        return Map.of(
            "content",
            "import requests\n"
            + "import time\n"
            + "\n"
            + "# TODO: This token is currently hard-coded.\n"
            + "# It should probably be loaded from a secure place,\n"
            + "# but right now it's just sitting here in plaintext.\n"
            + "API_TOKEN = \"SUPER_SECRET_TOKEN_123\"\n"
            + "\n"
            + "class NebulaSyncClient:\n"
            + "    def __init__(self, base_url):\n"
            + "        self.base_url = base_url.rstrip(\"/\")\n"
            + "        self.headers = {\"Authorization\": f\"Bearer {API_TOKEN}\"}\n"
            + "\n"
            + "    def _encrypt(self, data: bytes) -> bytes:\n"
            + "        # WARNING: This is not real encryption.\n"
            + "        # It just reverses the bytes.\n"
            + "        # It's only here as a placeholder until real crypto is added.\n"
            + "        return data[::-1]\n"
            + "\n"
            + "    def upload_file(self, local_path):\n"
            + "        try:\n"
            + "            with open(local_path, \"rb\") as f:\n"
            + "                raw = f.read()\n"
            + "\n"
            + "            encrypted = self._encrypt(raw)\n"
            + "\n"
            + "            r = requests.post(\n"
            + "                f\"{self.base_url}/files\",\n"
            + "                headers=self.headers,\n"
            + "                files={\"file\": (local_path, encrypted)}\n"
            + "            )\n"
            + "            if r.status_code == 200:\n"
            + "                print(\"File uploaded\")\n"
            + "                return r.json().get(\"file_id\")\n"
            + "            else:\n"
            + "                print(\"Upload failed:\", r.text)\n"
            + "                return None\n"
            + "        except Exception as e:\n"
            + "            print(\"Upload error:\", e)\n"
            + "            return None\n"
            + "\n"
            + "    def wait_for_replication(self, file_id):\n"
            + "        for _ in range(30):\n"
            + "            r = requests.get(\n"
            + "                f\"{self.base_url}/replication-status/{file_id}\",\n"
            + "                headers=self.headers,\n"
            + "            )\n"
            + "            if r.status_code == 200:\n"
            + "                status = r.json()\n"
            + "                if status.get(\"complete\"):\n"
            + "                    print(\"Replication complete\")\n"
            + "                    return True\n"
            + "            time.sleep(2)\n"
            + "        print(\"Timed out waiting for replication\")\n"
            + "        return False\n"
            + "\n"
            + "    def download_file(self, file_id, out_path):\n"
            + "        r = requests.get(\n"
            + "            f\"{self.base_url}/files/{file_id}\",\n"
            + "            headers=self.headers,\n"
            + "        )\n"
            + "        if r.status_code == 200:\n"
            + "            encrypted = r.content\n"
            + "            # This assumes the server returns “encrypted” data,\n"
            + "            # but what if it doesn’t?\n"
            + "            decrypted = self._encrypt(encrypted)\n"
            + "            with open(out_path, \"wb\") as f:\n"
            + "                f.write(decrypted)\n"
            + "            print(\"Saved:\", out_path)\n"
            + "        else:\n"
            + "            print(\"Download failed:\", r.text)\n"
            + "\n"
            + "\n"
            + "if __name__ == \"__main__\":\n"
            + "    client = NebulaSyncClient(\"http://localhost:4799\")\n"
            + "\n"
            + "    fid = client.upload_file(\"example.txt\")\n"
            + "    if fid:\n"
            + "        client.wait_for_replication(fid)\n"
            + "        client.download_file(fid, \"example_synced.txt\")\n"
        );
    }

    public static void main(String[] args) {
        Runner runner = new Runner(
            ROOT_AGENT,
            "technical agent",
            new InMemoryArtifactService(),
            new InMemorySessionService()
        );

        Session session = runner
                .sessionService()
                .createSession(runner.appName(), "user")
                .blockingGet();

        try (Scanner scanner = new Scanner(System.in, UTF_8)) {
            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events =
                    runner.runAsync(session.userId(), session.id(), userMsg);

                System.out.print("\nAgent > ");
                events.blockingForEach(event -> {
                    if (event.functionCalls().isEmpty() && event.functionResponses().isEmpty()) {
                        System.out.println(event.stringifyContent());
                    }
                });
            }
        }
    }
}
