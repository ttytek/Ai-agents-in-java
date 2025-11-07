package agents;

import com.google.adk.tools.Annotations.Schema;
import java.util.Map;
import java.util.HashMap;

/**
 * Provides functionality for updating a user's payment method in the system.
 * This class is designed to be registered as a tool for an ADK agent.
 */
public class PaymentMethod {

    /**
     * Updates the user's primary payment method using tokenized or partial details.
     * The agent MUST use this tool whenever the user asks to 'change their card', 
     * 'update their bank details', or 'set a new payment method'.
     * * @param userId The unique identifier for the user whose payment method is being updated.
     * @param methodType The type of payment method being used (e.g., "Visa", "MasterCard", "PayPal").
     * @param paymentToken A simulated secure token or the last 4 digits of the card/account number.
     * @return A map containing the update status and a confirmation message.
     */
    @Schema(description = "Updates the user's primary payment method with new details, requiring the user ID and payment token/type.")
    public static Map<String, String> paymentMethod(
        @Schema(name = "userId", description = "The unique identifier of the user (e.g., email or account number).")
        String userId,
        
        @Schema(name = "methodType", description = "The type of payment method, e.g., 'Visa', 'Amex', 'ACH'.")
        String methodType,
        
        @Schema(name = "paymentToken", description = "A secure token or identifier for the new payment method (e.g., last 4 digits of a card).")
        String paymentToken) {

        // --- Start Simulation/Mock Logic ---
        Map<String, String> response = new HashMap<>();

        // Basic validation
        if (userId == null || userId.isEmpty()) {
            response.put("status", "ERROR");
            response.put("message", "User ID is required for updating payment method.");
            return response;
        }

        if (paymentToken.length() < 4) {
            response.put("status", "ERROR");
            response.put("message", "Invalid payment token provided. Token must be at least 4 characters.");
            return response;
        }

        // Simulate success and data sanitization
        String maskedToken = paymentToken.substring(paymentToken.length() - 4);
        
        if (userId.equalsIgnoreCase("3003-C")) {
            // Simulate a user profile that rejects updates
            response.put("status", "FAILURE");
            response.put("message", "Payment processor denied the request for security review on this account.");
        } else {
            // Simulate successful update
            response.put("status", "SUCCESS");
            response.put("message", String.format(
                "Successfully updated payment method for user %s to %s ending in %s.",
                userId, methodType, maskedToken
            ));
        }
        
        // --- End Simulation/Mock Logic ---
        return response;
    }
}