package agents;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Scanner;
import io.reactivex.rxjava3.core.Flowable;

import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.adk.tools.FunctionTool;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;

public class AgentTeam {

    static BaseAgent technicalAgent = new TechnicalSpecialist().ROOT_AGENT;
    static BaseAgent billingAgent = new BillingSpecialist().ROOT_AGENT;

    public static final BaseAgent ROOT_AGENT = buildRootAgent();

    public static BaseAgent buildRootAgent() {

        if (technicalAgent == null || billingAgent == null) {
            System.err.println("❌ Cannot create root agent because one or more sub-agents failed to initialize.");
            return null;
        }

        return LlmAgent.builder()
            .name("agent team")
            .model("gemini-2.5-flash")
            .description("The main coordinator agent. Delegates requests to specialists.")
            .instruction(
                  "You are an agent coordinating two specialists. Your primary responsibility is to delegate requests to specialists. "
                + "The specialized sub-agents are: "
                + "1. 'technicalAgent': Handles technical requests, like troubleshooting and giving technical tips. "
                + "2. 'billingAgent': Handles billing-related requests like opening support tickets for billing issues. "
                + "Analyze the user's query. If it is related to technical aspects of a project, delegate it to technicalAgent. "
                + "If it is billing related, delegate it to billingAgent. "
                + "If the request falls out of those scopes, reply to the user that their request can't be handled."
            )
            .subAgents(technicalAgent, billingAgent)
            .build();
    }

    public static void main(String[] args) {
        Runner runner = new Runner(
            ROOT_AGENT,
            "agent team",
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
