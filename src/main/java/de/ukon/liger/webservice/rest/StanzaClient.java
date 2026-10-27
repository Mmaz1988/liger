package de.ukon.liger.webservice.rest;

import de.ukon.liger.syntax.LinguisticStructure;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Stanza service (xleplusglue Stanza/main.py): UD dependency parses as LiGER structures.
 * {@code STANZA_URL}, default {@code http://localhost:8085}. A parse that fails says why; it
 * never falls back to XLE.
 */
public final class StanzaClient {
    private static final RestTemplate REST = new RestTemplate();

    private StanzaClient() {
    }

    public static String url() {
        String url = System.getenv("STANZA_URL");
        return url == null || url.isBlank() ? "http://localhost:8085" : url.replaceAll("/+$", "");
    }

    /** The installed UD models: {@code id}, {@code language}, {@code package} each. */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> models() {
        List<Map<String, Object>> models = REST.getForObject(url() + "/models", List.class);
        return models == null ? List.of() : models;
    }

    /** One sentence's parse; {@code IllegalStateException} with Stanza's reason if it fails. */
    @SuppressWarnings("unchecked")
    public static LinguisticStructure parse(String sentenceId, String sentence, String model) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("sentence", sentence);
        request.put("model", model);
        request.put("sentenceId", sentenceId == null ? "S0" : sentenceId);
        try {
            LinkedHashMap<String, Object> structure = REST.postForObject(url() + "/parse_to_liger", request,
                    LinkedHashMap.class);
            if (structure == null) {
                throw new IllegalStateException("Stanza returned no parse for \"" + sentence + "\".");
            }
            return LinguisticStructure.parseFromJson(structure);
        } catch (HttpStatusCodeException e) {
            throw new IllegalStateException("Stanza could not parse \"" + sentence + "\": "
                    + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new IllegalStateException("The Stanza service at " + url() + " is not reachable: "
                    + e.getMessage(), e);
        }
    }
}
