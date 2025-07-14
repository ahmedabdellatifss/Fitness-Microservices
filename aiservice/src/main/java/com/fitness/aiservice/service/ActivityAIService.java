package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity)
    {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("Response from gemini service: {}", aiResponse);

        return processAiResponse(activity, aiResponse);
    }

    private Recommendation processAiResponse(Activity activity, String aiResponse)
    {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```","")
                    .trim();

            //log.info("Passed Response from Ai: {} ", jsonContent);
            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafetyGuideLines(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();

        }catch (Exception e){
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity)
    {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routing"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafetyGuideLines(JsonNode safetyNode)
    {
        List<String> safety = new ArrayList<>();
        if(safetyNode.isArray())
        {
            safetyNode.forEach( item ->{
                safety.add(item.asText());
            });

        }
        return safety.isEmpty() ?
                Collections.singletonList("Follow general safety guideline") :
                safety;
    }

    private List<String> extractSuggestions(JsonNode suggestions)
    {
        List<String> suggestionList = new ArrayList<>();
        if(suggestions.isArray())
        {
            suggestions.forEach(suggestion->{
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();

                suggestionList.add(String.format("%s: %s", workout, description));
            });

        }
        return suggestionList.isEmpty() ?
                Collections.singletonList("No specific suggestion provided") :
                suggestionList;
    }

    private List<String> extractImprovements(JsonNode improvements)
    {
        List<String> improvementList = new ArrayList<>();
        if(improvements.isArray())
        {
            improvements.forEach(improvement->{
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();

                improvementList.add(String.format("%s: %s", area, detail));

            });
        }

        return improvementList.isEmpty() ?
                Collections.singletonList("No specific improvement provided") :
                improvementList;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode,String key, String prefix )
    {
        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                Analyze this fitness activity and provide detailed recommendations in the following EXACT JASON format:
                {
                    "analysis": {
                        "overall": "Overall analysis here",
                        "pace": "pace analysis here",
                        "heartRate": "heart rate analysis here",
                        "calories": "calories analysis here"
                    },
                    "improvements": [
                        {
                            "area": "Area name",
                            "recommendation": "Detailed recommendation"
                        }
                    ],
                    "suggestions": [
                        {
                            "workout": "Workout name",
                            "description": "description of the Workout"
                        }
                    ],
                    "safety": [
                        "Safety point 1",
                        "Safety point 2"
                    ]
                }
                
                Analyze this activity:
                Activity Type: %s
                Duration: %d minutes
                Calories Burned: %d
                Additional Metrics: %s
                
                Provide detailed analysis focusing on performance, improvement, next workout suggestions, and safety guidelines.
                Ensure the response follows the EXACT JASON format shown above:
                """,
                        activity.getType(),
                        activity.getDuration(),
                        activity.getCaloriesBurned(),
                        activity.getAdditionalMetrics()
        );

    }
}
