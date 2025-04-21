package douglas.utils;

import douglas.repository.FlightRepository;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class DataGenerator {
    private final FlightRepository flightRepository;
    private static final int BATCH_SIZE = 25;

    public DataGenerator(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    private static final String[] BRAZILIAN_AIRPORTS = {
            "GRU", "GIG", "BSB", "CNF", "SSA", "POA", "REC", "FOR", "BEL", "RAO",
            "VCP", "CWB", "MAO", "FLN", "GYN", "NAT", "CGB", "BVB", "THE", "UDI",
            "JPA", "MCZ", "MCP", "PET", "IGU", "CGR", "AJU", "JDO", "IMP", "SLZ"
    };

    private static final Set<String> HUBS = Set.of("GRU", "GIG", "POA", "BSB", "CNF", "REC", "SSA", "VCP", "FOR");

    private static final String[] BRAZILIAN_AIRLINES = {
            "GOL", "LATAM", "AZUL"
    };

    public void populate() {
        int routes = 870;
        Random random = new Random();
        Set<String> uniqueRoutes = generateRoutes(routes);

        uniqueRoutes.parallelStream()
            .forEach(route -> {
                List<Map<String, AttributeValue>> flights = new ArrayList<>();
                String[] parts = route.split("-");
                int dynamicFares = getFaresPerRoute(parts[0], parts[1]);
                for (int i = 0; i < dynamicFares; i++) {
                flights.add(generateRandomFare(route, random));
                if (flights.size() > BATCH_SIZE) {
                    flightRepository.putFare(flights);
                    flights.clear();
                }
            }
            if (!flights.isEmpty()) {
                flightRepository.putFare(flights);
            }
        });
    }

    private Set<String> generateRoutes(int routes) {
        Set<String> uniqueRoutes = new HashSet<>();
        Random random = new Random();

        do {
            String origin = BRAZILIAN_AIRPORTS[random.nextInt(BRAZILIAN_AIRPORTS.length)];
            String destination = BRAZILIAN_AIRPORTS[random.nextInt(BRAZILIAN_AIRPORTS.length)];
            if (!origin.equals(destination)) {
                uniqueRoutes.add(origin + "-" + destination);
            }
        }
        while (uniqueRoutes.size() < routes);

        return uniqueRoutes;
    }

    private int getFaresPerRoute(String origin, String destination) {
        boolean isHub = HUBS.contains(origin) || HUBS.contains(destination);

        if (isHub) return 20;
        return 5;
    }

    private Map<String, AttributeValue> generateRandomFare( String route, Random random) {
        BigDecimal price = BigDecimal.valueOf(200 + random.nextInt(2801));

        Instant expiryTime = Instant.now().plusSeconds(random.nextInt(48 * 3600));

        return Map.of(
                "route", AttributeValue.fromS(route),
                "expiryTime", AttributeValue.fromN(String.valueOf(expiryTime.getEpochSecond())),
                "price", AttributeValue.fromN(price.toString()),
                "currency", AttributeValue.fromS("BRL"),
                "airline", AttributeValue.fromS(BRAZILIAN_AIRLINES[random.nextInt(BRAZILIAN_AIRLINES.length)])
        );
    }
}
