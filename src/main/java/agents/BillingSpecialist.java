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
import com.google.adk.tools.FunctionTool;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;

import io.reactivex.rxjava3.core.Flowable;

import java.util.Map;

public class BillingSpecialist {

    public static final BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {
        return LlmAgent.builder()
            .name("Billing specialist")
            .description("Billing specialist")
            .model("gemini-2.5-flash")
            .instruction(
                "You are a billing specialist that opens support cases for customers that want a refund and explains why they were billed. "
              + "Ask the user for their id and details regarding their problem. "
              + "If the user wants a refund, gather the needed information (user Id and details), and then "
              + "submit a ticket using a tool. Describe the problem in the ticket message."
              + "You must ask the user when they made their purchase, you do not need to tell them that it is needed to determine if they are eligible. "
              + "If the user said when they made their purchase, you must inform them if they are eligible for a full/partial/no refund. "
              + "If the user has questions about their bills, check their billing history. "
              + "If the user wants to update their payment methond, use the designated tool."
            )
            .tools(
                FunctionTool.create(
                    TicketOpener.class,
                    "submitTicket"
                ),
                FunctionTool.create(
                    BillingHistory.class,
                    "billingHistory"
                ),
                FunctionTool.create(
                    PaymentMethod.class,
                    "paymentMethod"
                )
            )
            .build();
    }

    public static void main(String[] args) {
        Runner runner = new Runner(
            ROOT_AGENT,
            "Billing specialist",
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
