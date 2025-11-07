package agents;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.io.IOException;
import java.util.Map;

import com.google.adk.tools.Annotations.Schema;


public class TicketOpener {
    private static final String LOG_FILE_NAME = "tickets.log";

    @Schema(description = "Submits a new refund support ticket and sends user a mail with form to fill ."
                          + "The refunds policy is as follows: "
                          + "if made withing 7 days of purchase, the user is eligible for a full refund "
                          + "if made withing 30 days of purchase, the user is eligible for a partial refund "
                          + "if more than 30 days havepassed since the purchase, a refund cannot be made."
    )
    public static Map<String, String> submitTicket (
        @Schema(name = "userId", description = "The unique identifier for the user.")
        String userId,

        @Schema(name = "ticketMessage", description = "The contents of the ticket")
        String ticketMessage) {

        
        Path filePath = Path.of(LOG_FILE_NAME);
        String logEntry = String.format("[%s] - %s%n", userId, ticketMessage);

        try {
            // Write to the file, creating it if it doesn't exist, and appending to the end.
            Files.writeString(
                filePath, 
                logEntry, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND
            );

            // Send the user a form to fill

            // Return a simple Map for the LLM to use in its final response
            Map<String, String> result = new HashMap<>();
            result.put("status", "SUCCESS");
            result.put("confirmation", "Data successfully saved for user " + userId + ".");
            return result;

        } catch (IOException e) {
            System.err.println("File write error: " + e.getMessage());
            Map<String, String> result = new HashMap<>();
            result.put("status", "ERROR");
            result.put("confirmation", "A system error prevented saving the data: " + e.getMessage());
            return result;
        }
    }

}
