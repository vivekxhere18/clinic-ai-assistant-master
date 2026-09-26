package com.clinic.aiassistant.service;

import com.clinic.aiassistant.dto.AiSummary;
import com.clinic.aiassistant.dto.PatientRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a structured, non-diagnostic AI summary for a patient intake form.
 *
 * <p>When an Anthropic API key is configured, calls Claude and parses the JSON response.
 * On any missing key / HTTP failure / malformed response, falls back to a deterministic
 * mock summary so the demo always works end-to-end.</p>
 */
@Service
public class AiSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AiSummaryService.class);

    private static final List<String> RED_FLAG_KEYWORDS = Arrays.asList(
            "chest pain", "breathless", "breathing difficulty", "difficulty breathing",
            "shortness of breath", "severe", "blood", "bleeding", "unconscious",
            "fainted", "faint", "seizure", "high fever", "unresponsive", "poison", "suicidal"
    );

    private static final String DISCLAIMER =
            "This summary is generated for informational purposes only and is not a medical " +
            "diagnosis or treatment recommendation. Please consult a qualified doctor for " +
            "diagnosis, prescriptions, or treatment.";

    private final String apiKey;
    private final String model;
    private final String version;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiSummaryService(@Value("${anthropic.api-key:}") String apiKey,
                             @Value("${anthropic.model:claude-sonnet-4-6}") String model,
                             @Value("${anthropic.base-url:https://api.anthropic.com/v1/messages}") String baseUrl,
                             @Value("${anthropic.version:2023-06-01}") String version) {
        this.apiKey = apiKey;
        this.model = model;
        this.version = version;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public AiSummary generateSummary(PatientRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("No ANTHROPIC_API_KEY configured — using mock AI summary.");
            return buildMockSummary(request);
        }

        try {
            String rawResponse = callAnthropic(request);
            AiSummary summary = parseAiResponse(rawResponse);
            summary.setStatus(AiSummary.STATUS_OK);
            return summary;
        } catch (Exception e) {
            log.warn("AI summary generation failed, falling back to mock summary: {}", e.getMessage());
            AiSummary fallback = buildMockSummary(request);
            fallback.setStatus(AiSummary.STATUS_FALLBACK);
            return fallback;
        }
    }

    private String callAnthropic(PatientRequest request) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request);

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 1024,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        JsonNode response = restClient.post()
                .header("x-api-key", apiKey)
                .header("anthropic-version", version)
                .header("content-type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("content") || response.get("content").isEmpty()) {
            throw new IllegalStateException("Empty response from Anthropic API");
        }
        return response.get("content").get(0).get("text").asText();
    }

    private String buildSystemPrompt() {
        return "You are a clinical intake assistant for a small clinic in India. " +
                "You MUST NOT diagnose any condition, MUST NOT prescribe medication, and " +
                "MUST NOT suggest dosages. Your output is informational only, to help clinic " +
                "staff prepare for a doctor's visit. The 'attentionLevel' field indicates ONLY " +
                "triage urgency (Routine, Soon, or Urgent) — it must never name or imply a " +
                "specific medical condition. Mark attentionLevel as 'Urgent' if the patient " +
                "reports red-flag symptoms such as chest pain, difficulty breathing, severe " +
                "bleeding, loss of consciousness, or similarly serious symptoms — without " +
                "naming any condition. " +
                "Respond with ONLY valid JSON (no markdown, no code fences, no prose) matching " +
                "exactly this schema: " +
                "{\"patientSummary\": string, \"symptomsIdentified\": string[], " +
                "\"duration\": string, \"missingInformation\": string[], " +
                "\"suggestedQuestions\": string[], \"attentionLevel\": \"Routine\"|\"Soon\"|\"Urgent\", " +
                "\"disclaimer\": string}";
    }

    private String buildUserPrompt(PatientRequest request) {
        return "Patient intake data:\n" +
                "Name: " + safe(request.getName()) + "\n" +
                "Age: " + request.getAge() + "\n" +
                "Gender: " + safe(request.getGender()) + "\n" +
                "City: " + safe(request.getCity()) + "\n" +
                "Medical history: " + safe(request.getMedicalHistory()) + "\n" +
                "Selected symptoms: " + String.join(", ", request.getSymptoms()) + "\n" +
                "Additional symptom description: " + safe(request.getSymptomText()) + "\n" +
                "Duration: " + safe(request.getDuration()) + "\n" +
                "Additional notes: " + safe(request.getNotes()) + "\n\n" +
                "Produce the structured JSON summary as instructed.";
    }

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "Not provided" : value;
    }

    /**
     * Extracts the first JSON object from a (possibly noisy) model response and maps it
     * onto {@link AiSummary}, validating required fields.
     */
    private AiSummary parseAiResponse(String rawText) throws Exception {
        String json = extractJsonObject(rawText);
        JsonNode node = objectMapper.readTree(json);

        AiSummary summary = new AiSummary();
        summary.setPatientSummary(textOrThrow(node, "patientSummary"));
        summary.setSymptomsIdentified(toStringList(node.get("symptomsIdentified")));
        summary.setDuration(textOrThrow(node, "duration"));
        summary.setMissingInformation(toStringList(node.get("missingInformation")));
        summary.setSuggestedQuestions(toStringList(node.get("suggestedQuestions")));

        String level = textOrThrow(node, "attentionLevel");
        if (!level.equalsIgnoreCase(AiSummary.LEVEL_ROUTINE)
                && !level.equalsIgnoreCase(AiSummary.LEVEL_SOON)
                && !level.equalsIgnoreCase(AiSummary.LEVEL_URGENT)) {
            throw new IllegalStateException("Invalid attentionLevel: " + level);
        }
        summary.setAttentionLevel(capitalize(level));

        String disclaimer = node.has("disclaimer") && !node.get("disclaimer").asText().isBlank()
                ? node.get("disclaimer").asText()
                : DISCLAIMER;
        summary.setDisclaimer(disclaimer);

        return summary;
    }

    private String textOrThrow(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).asText().isBlank()) {
            throw new IllegalStateException("Missing required field: " + field);
        }
        return node.get(field).asText();
    }

    private List<String> toStringList(JsonNode arrayNode) {
        List<String> values = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(n -> values.add(n.asText()));
        }
        return values;
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private String extractJsonObject(String text) {
        Matcher matcher = Pattern.compile("\\{[\\s\\S]*\\}").matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new IllegalStateException("No JSON object found in AI response");
    }

    /**
     * Deterministic, rule-based mock summary used when no API key is configured or when
     * the real AI call fails. Ensures the demo always works end-to-end.
     */
    private AiSummary buildMockSummary(PatientRequest request) {
        List<String> symptoms = new ArrayList<>(request.getSymptoms());
        if (request.getSymptomText() != null && !request.getSymptomText().isBlank()) {
            symptoms.add(request.getSymptomText().trim());
        }

        String combinedText = (String.join(" ", symptoms) + " " +
                safe(request.getSymptomText()) + " " + safe(request.getNotes())).toLowerCase(Locale.ROOT);

        boolean isRedFlag = RED_FLAG_KEYWORDS.stream().anyMatch(combinedText::contains);
        String attentionLevel = isRedFlag ? AiSummary.LEVEL_URGENT : AiSummary.LEVEL_ROUTINE;

        String patientSummary = String.format(
                "%s (age %d) reports %s for a duration of %s.%s",
                safe(request.getName()), request.getAge() == null ? 0 : request.getAge(),
                symptoms.isEmpty() ? "unspecified symptoms" : String.join(", ", symptoms).toLowerCase(Locale.ROOT),
                safe(request.getDuration()),
                isRedFlag ? " Reported symptoms include potential red-flag indicators that warrant prompt attention."
                        : ""
        );

        List<String> missingInfo = new ArrayList<>();
        if (request.getMedicalHistory() == null || request.getMedicalHistory().isBlank()) {
            missingInfo.add("Past medical history / existing conditions");
        }
        missingInfo.add("Any current medications or allergies");
        missingInfo.add("Severity of symptoms on a scale of 1-10");
        missingInfo.add("Whether symptoms are worsening, stable, or improving");

        List<String> questions = new ArrayList<>(List.of(
                "When exactly did the symptoms start, and did they begin suddenly or gradually?",
                "Have you taken any medication or home remedy for this already?",
                "Is there any family history relevant to these symptoms?",
                "Are there any other symptoms not yet mentioned (e.g. fatigue, appetite changes)?"
        ));
        if (isRedFlag) {
            questions.add(0, "Please describe the severity and exact nature of the reported red-flag symptom(s) immediately.");
        }

        return new AiSummary(
                patientSummary,
                symptoms.isEmpty() ? List.of("Not specified") : symptoms,
                safe(request.getDuration()),
                missingInfo,
                questions,
                attentionLevel,
                DISCLAIMER,
                AiSummary.STATUS_FALLBACK
        );
    }
}