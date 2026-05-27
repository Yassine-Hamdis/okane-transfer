package com.okanetransfer.service;

import com.okanetransfer.dto.request.SendMessageRequest;
import com.okanetransfer.dto.request.StartConversationRequest;
import com.okanetransfer.dto.response.SendMessageResponse;
import com.okanetransfer.dto.response.StartConversationResponse;
import com.okanetransfer.entity.ChatbotConversation;
import com.okanetransfer.entity.ChatbotMessage;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.*;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.ChatbotConversationRepository;
import com.okanetransfer.repository.ChatbotMessageRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:application.properties")
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    // Keywords for intent detection
    private static final List<String> TRACKING_KEYWORDS = List.of(
            "track", "status", "where", "code", "transfer",
            "suivi", "statut", "où", "transfert",
            "تتبع", "حالة", "أين", "تحويل"
    );

    private static final String WELCOME_MESSAGE_FR =
            "Bonjour ! Je suis l'assistant OkaneTransfer. " +
                    "Je peux vous aider avec le suivi de vos transferts, " +
                    "les frais, les délais et plus encore. Comment puis-je vous aider ?";

    private static final String WELCOME_MESSAGE_EN =
            "Hello! I'm the OkaneTransfer assistant. " +
                    "I can help you with transfer tracking, fees, delays and more. " +
                    "How can I help you?";

    private static final String WELCOME_MESSAGE_AR =
            "مرحباً! أنا مساعد OkaneTransfer. " +
                    "يمكنني مساعدتك في تتبع التحويلات والرسوم والمواعيد وأكثر. " +
                    "كيف يمكنني مساعدتك؟";

    private static final String ESCALATION_MESSAGE_FR =
            "Je vais vous mettre en contact avec un agent humain. " +
                    "Veuillez patienter ou visiter l'agence la plus proche.";

    private static final String ESCALATION_MESSAGE_EN =
            "I'll connect you with a human agent. " +
                    "Please wait or visit your nearest agency.";

    private static final String ESCALATION_MESSAGE_AR =
            "سأقوم بتحويلك إلى وكيل بشري. " +
                    "يرجى الانتظار أو زيارة أقرب وكالة.";

    @Value("${gpt.api.key:}")
    private String gptApiKey;

    @Value("${gpt.api.url:https://api.openai.com/v1/chat/completions}")
    private String gptApiUrl;

    @Value("${gpt.model:gpt-3.5-turbo}")
    private String gptModel;

    @Autowired
    private ChatbotConversationRepository conversationRepository;

    @Autowired
    private ChatbotMessageRepository messageRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private UserRepository userRepository;

    private String contextContent = null;

    // ─────────────────────────────────────────────────────
    //  START CONVERSATION
    // ─────────────────────────────────────────────────────

    @Transactional
    public StartConversationResponse startConversation(Long userId,
                                                       StartConversationRequest request) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        String sessionId = UUID.randomUUID().toString();
        ChatLanguage language = request.getLanguage() != null
                ? request.getLanguage() : ChatLanguage.FR;

        ChatbotConversation conversation = ChatbotConversation.builder()
                .user(user)
                .sessionId(sessionId)
                .language(language)
                .status(ConversationStatus.ACTIVE)
                .build();

        conversationRepository.save(conversation);

        String welcome = getWelcomeMessage(language);

        // Save welcome message as first BOT message
        saveMessage(conversation, MessageSender.BOT, welcome, MessageIntent.FAQ);

        return StartConversationResponse.builder()
                .conversationId(conversation.getId())
                .sessionId(sessionId)
                .welcomeMessage(welcome)
                .build();
    }

    // ─────────────────────────────────────────────────────
    //  SEND MESSAGE
    // ─────────────────────────────────────────────────────

    @Transactional
    public SendMessageResponse sendMessage(SendMessageRequest request,
                                           Long userId) {
        ChatbotConversation conversation = conversationRepository
                .findBySessionId(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + request.getSessionId()));

        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Conversation is not active. Status: " + conversation.getStatus());
        }

        // ── 1. Save user message ─────────────────────
        saveMessage(conversation, MessageSender.USER,
                request.getContent(), null);

        // ── 2. Detect intent ─────────────────────────
        MessageIntent intent = detectIntent(request.getContent());

        // ── 3. Generate bot response ──────────────────
        String botResponse;
        boolean escalated = false;

        switch (intent) {
            case TRACKING -> {
                botResponse = handleTracking(request.getContent(),
                        conversation.getLanguage());
            }
            case ESCALATION -> {
                botResponse = getEscalationMessage(conversation.getLanguage());
                escalateConversation(conversation);
                escalated = true;
            }
            default -> {
                // FAQ — use GPT with context
                botResponse = handleFaq(request.getContent(),
                        conversation.getLanguage());
                // Re-evaluate: if GPT couldn't answer, escalate
                if (shouldEscalate(botResponse)) {
                    intent = MessageIntent.ESCALATION;
                    botResponse = getEscalationMessage(conversation.getLanguage());
                    escalateConversation(conversation);
                    escalated = true;
                }
            }
        }

        // ── 4. Save bot response ──────────────────────
        saveMessage(conversation, MessageSender.BOT, botResponse, intent);

        return SendMessageResponse.builder()
                .content(botResponse)
                .sender(MessageSender.BOT)
                .intent(intent)
                .escalated(escalated)
                .build();
    }

    // ─────────────────────────────────────────────────────
    //  CLOSE CONVERSATION
    // ─────────────────────────────────────────────────────

    @Transactional
    public void closeConversation(String sessionId) {
        ChatbotConversation conversation = conversationRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + sessionId));
        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    // ─────────────────────────────────────────────────────
    //  QUERIES
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ChatbotConversation> getEscalated() {
        return conversationRepository.findAllByStatus(ConversationStatus.ESCALATED);
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessage> getMessages(String sessionId) {
        ChatbotConversation conversation = conversationRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + sessionId));
        return messageRepository
                .findAllByConversationIdOrderBySentAtAsc(conversation.getId());
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Intent Detection
    // ─────────────────────────────────────────────────────

    /**
     * Detects intent from user message using keyword matching.
     *
     * Logic:
     *  - If message contains a tracking keyword → TRACKING
     *  - Everything else goes to FAQ (GPT handles it)
     *  - If GPT fails or says it doesn't know → ESCALATION
     */
    private MessageIntent detectIntent(String message) {
        String lower = message.toLowerCase();

        boolean isTracking = TRACKING_KEYWORDS.stream()
                .anyMatch(lower::contains);

        // Also check for 8-char code pattern e.g. "A3F7K9P2"
        boolean hasWithdrawalCode = message.matches(".*\\b[A-Z0-9]{8}\\b.*");

        if (isTracking || hasWithdrawalCode) {
            return MessageIntent.TRACKING;
        }

        return MessageIntent.FAQ;
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Transfer Tracking Handler
    // ─────────────────────────────────────────────────────

    private String handleTracking(String message, ChatLanguage language) {
        // Extract 8-char alphanumeric code from message
        String code = extractWithdrawalCode(message);

        if (code == null) {
            return switch (language) {
                case FR -> "Veuillez fournir votre code de retrait " +
                        "(8 caractères, ex: A3F7K9P2).";
                case EN -> "Please provide your withdrawal code " +
                        "(8 characters, e.g. A3F7K9P2).";
                case AR -> "يرجى تقديم رمز السحب الخاص بك " +
                        "(8 أحرف، مثال: A3F7K9P2).";
            };
        }

        Optional<Transfer> transferOpt =
                transferRepository.findByWithdrawalCode(code.toUpperCase());

        if (transferOpt.isEmpty()) {
            return switch (language) {
                case FR -> "Aucun transfert trouvé avec le code: " + code +
                        ". Vérifiez le code et réessayez.";
                case EN -> "No transfer found with code: " + code +
                        ". Please check the code and try again.";
                case AR -> "لم يتم العثور على تحويل بالرمز: " + code +
                        ". يرجى التحقق من الرمز والمحاولة مجدداً.";
            };
        }

        Transfer transfer = transferOpt.get();
        return buildTrackingResponse(transfer, language);
    }

    private String buildTrackingResponse(Transfer transfer, ChatLanguage language) {
        String status = transfer.getStatus().name();
        String recipient = transfer.getRecipientFirstName() + " "
                + transfer.getRecipientLastName();
        String amount = transfer.getReceivedAmount() + " "
                + transfer.getReceivedCurrency().getCode();

        return switch (language) {
            case FR -> String.format(
                    "Transfert %s:\n" +
                            "• Destinataire: %s\n" +
                            "• Montant: %s\n" +
                            "• Statut: %s\n" +
                            "• Expire le: %s",
                    transfer.getWithdrawalCode(), recipient, amount,
                    translateStatus(status, language),
                    transfer.getExpiresAt().toLocalDate());

            case EN -> String.format(
                    "Transfer %s:\n" +
                            "• Recipient: %s\n" +
                            "• Amount: %s\n" +
                            "• Status: %s\n" +
                            "• Expires: %s",
                    transfer.getWithdrawalCode(), recipient, amount,
                    translateStatus(status, language),
                    transfer.getExpiresAt().toLocalDate());

            case AR -> String.format(
                    "التحويل %s:\n" +
                            "• المستفيد: %s\n" +
                            "• المبلغ: %s\n" +
                            "• الحالة: %s\n" +
                            "• تنتهي في: %s",
                    transfer.getWithdrawalCode(), recipient, amount,
                    translateStatus(status, language),
                    transfer.getExpiresAt().toLocalDate());
        };
    }

    private String translateStatus(String status, ChatLanguage language) {
        return switch (status) {
            case "EN_ATTENTE" -> switch (language) {
                case FR -> "En attente de retrait";
                case EN -> "Waiting for pickup";
                case AR -> "في انتظار الاستلام";
            };
            case "PAYE" -> switch (language) {
                case FR -> "Payé";
                case EN -> "Paid out";
                case AR -> "تم الدفع";
            };
            case "ANNULE" -> switch (language) {
                case FR -> "Annulé";
                case EN -> "Cancelled";
                case AR -> "ملغى";
            };
            case "EXPIRE" -> switch (language) {
                case FR -> "Expiré";
                case EN -> "Expired";
                case AR -> "منتهي الصلاحية";
            };
            default -> status;
        };
    }

    private String extractWithdrawalCode(String message) {
        // Match exactly 8 uppercase alphanumeric characters
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("\\b([A-Z0-9]{8})\\b");
        java.util.regex.Matcher matcher =
                pattern.matcher(message.toUpperCase());
        return matcher.find() ? matcher.group(1) : null;
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — GPT FAQ Handler
    // ─────────────────────────────────────────────────────

    private String handleFaq(String userMessage, ChatLanguage language) {
        if (gptApiKey == null || gptApiKey.isBlank()) {
            log.warn("GPT API key not configured — using fallback response");
            return getFallbackResponse(language);
        }

        try {
            String context = loadContext();
            String systemPrompt = buildSystemPrompt(context, language);
            String response = callGptApi(systemPrompt, userMessage);
            return response;
        } catch (Exception e) {
            log.error("GPT API call failed: {}", e.getMessage());
            return getFallbackResponse(language);
        }
    }

    private String buildSystemPrompt(String context, ChatLanguage language) {
        String langInstruction = switch (language) {
            case FR -> "Réponds toujours en français.";
            case EN -> "Always respond in English.";
            case AR -> "أجب دائماً باللغة العربية.";
        };

        return "You are a helpful customer support assistant for OkaneTransfer, " +
                "a money transfer platform. " +
                langInstruction + "\n\n" +
                "Use ONLY the following knowledge base to answer questions. " +
                "If the answer is not in the knowledge base, say you don't know " +
                "and suggest speaking to a human agent.\n\n" +
                "KNOWLEDGE BASE:\n" + context;
    }

    private String callGptApi(String systemPrompt,
                              String userMessage) throws Exception {
        String requestBody = "{"
                + "\"model\":\"" + gptModel + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":" + jsonEscape(systemPrompt) + "},"
                + "{\"role\":\"user\",\"content\":" + jsonEscape(userMessage) + "}"
                + "],"
                + "\"max_tokens\":300,"
                + "\"temperature\":0.3"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gptApiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GPT API error: " + response.statusCode());
        }

        return parseGptResponse(response.body());
    }

    /**
     * Simple JSON parser to extract GPT response content.
     * Avoids adding a JSON library dependency just for this.
     */
    private String parseGptResponse(String json) {
        // Extract content from: "choices":[{"message":{"content":"..."}}]
        int contentIndex = json.indexOf("\"content\":");
        if (contentIndex == -1) return null;

        int start = json.indexOf("\"", contentIndex + 10) + 1;
        int end   = json.indexOf("\"", start);
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }

    private String jsonEscape(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }

    private String loadContext() {
        if (contextContent != null) return contextContent;
        try {
            ClassPathResource resource =
                    new ClassPathResource("chatbot-context.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            resource.getInputStream(), StandardCharsets.UTF_8))) {
                contextContent = reader.lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.error("Failed to load chatbot context file: {}", e.getMessage());
            contextContent = "OkaneTransfer is a money transfer platform.";
        }
        return contextContent;
    }

    private String getFallbackResponse(ChatLanguage language) {
        return switch (language) {
            case FR -> "Je ne peux pas répondre à cette question pour le moment. " +
                    "Veuillez contacter un agent humain ou visiter votre agence.";
            case EN -> "I cannot answer this question right now. " +
                    "Please contact a human agent or visit your nearest agency.";
            case AR -> "لا يمكنني الإجابة على هذا السؤال الآن. " +
                    "يرجى التواصل مع وكيل بشري أو زيارة أقرب وكالة.";
        };
    }

    private boolean shouldEscalate(String botResponse) {
        if (botResponse == null) return true;
        String lower = botResponse.toLowerCase();
        return lower.contains("don't know") ||
                lower.contains("cannot answer") ||
                lower.contains("ne sais pas") ||
                lower.contains("لا أعرف");
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Escalation
    // ─────────────────────────────────────────────────────

    private void escalateConversation(ChatbotConversation conversation) {
        conversation.setStatus(ConversationStatus.ESCALATED);
        conversationRepository.save(conversation);
        log.info("Conversation {} escalated to human agent",
                conversation.getSessionId());
    }

    private String getEscalationMessage(ChatLanguage language) {
        return switch (language) {
            case FR -> ESCALATION_MESSAGE_FR;
            case EN -> ESCALATION_MESSAGE_EN;
            case AR -> ESCALATION_MESSAGE_AR;
        };
    }

    private String getWelcomeMessage(ChatLanguage language) {
        return switch (language) {
            case FR -> WELCOME_MESSAGE_FR;
            case EN -> WELCOME_MESSAGE_EN;
            case AR -> WELCOME_MESSAGE_AR;
        };
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Persist Messages
    // ─────────────────────────────────────────────────────

    private void saveMessage(ChatbotConversation conversation,
                             MessageSender sender,
                             String content,
                             MessageIntent intent) {
        ChatbotMessage message = ChatbotMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .intent(intent)
                .build();
        messageRepository.save(message);
    }
}