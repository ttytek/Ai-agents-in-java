# README

This is my implementation of two conversational AI agents in Java.

## Agent A – Technical Specialist
This agent answers technical questions about a made-up project called **Nebula**.  
Its capabilities include:

1. Reading troubleshooting notes  
2. Reading integration tips  
3. Reading source code of the project

### Example questions and answers for Agent A:
**Q:** Why are my files corrupted?  
**A:** The troubleshooting notes indicate that if files seem corrupted after sync and logs show **"Block verification failed"**, it could be due to a node running an older hashing scheme (all nodes must run version **2.4 or newer**) or the cache directory being on a failing disk. You can try running `fsck` or changing `cache_dir` to an SSD-backed path such as `/mnt/storage_ssd`.

**Q:** How is encryption handled?  
**A:** The source code reveals that the current implementation of encryption is not real encryption. The `_encrypt` method in the `NebulaSyncClient` class is a placeholder that simply reverses the bytes of the data. The source code explicitly states:  
`# WARNING: This is not real encryption. It just reverses the bytes. It's only here as a placeholder until real crypto is added.`

The answers are entirely based on information found in the sources available to the model.

---

## Agent B – Billing Specialist
This agent handles requests related to billing.  
Its capabilities are implemented using tool calling:

1. Submitting a refund ticket and informing the user if they are eligible for a refund.  
   For testing, this tool writes the ticket message and user ID to **tickets.log**.
2. Checking a user’s payment history and explaining payments.  
3. Changing a user’s payment method.

Each request is handled by the appropriate agent, chosen automatically. The agents can also collaborate within a simple conversation.

### Example conversation flow for Agent B:
1. **User:** I want to get a refund  
2. **Agent:** asks for user ID and reason for the refund  
3. **User:** My ID is 1234, I am not satisfied with the product.  
4. **Agent:** asks when the purchase was made  
5. **User:** I made the purchase two days ago  
6. **Agent:** responds that the user is eligible for a full refund and that they will receive an email with a form to fill out

After this conversation, the `tickets.log` file should contain something like:
[1234] - User 1234 is requesting a refund because they are not satisfied with the product.

You can also test the agent by asking it to change your payment info or explain why you were billed.  
Functions handling these tool calls are in **BillingHistory.java** and **PaymentMethod.java**.

For checking transaction history use user ID **1001-A** or **2002-B**.  
When changing payment method, use ID **3003-C** to simulate a failure, or any other ID to simulate a successful payment method change.

---

## Running the project

You need to set an environment variable `GOOGLE_API_KEY` to a valid key for the project to run successfully.

To run the project, use the following commands:

```bash
mvn compile
mvn package
mvn exec:java -Dexec.mainClass="agents.AgentTeam"

