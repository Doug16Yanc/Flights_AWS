package douglas.repository;

import douglas.model.Flight;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Singleton
public class FlightRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "flights";

    public FlightRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void putFare(List<Map<String, AttributeValue>> flights) {

        Set<String> uniqueKeys = new HashSet<>();
        List<Map<String, AttributeValue>> uniqueFlights = new ArrayList<>();

        for (Map<String, AttributeValue> flight : flights) {
            String route = flight.get("route").s();
            String expiryTime = flight.get("expiryTime").n();

            String key = route + ":" + expiryTime;

            if (!uniqueKeys.contains(key)) {
                uniqueKeys.add(key);
                uniqueFlights.add(flight);
            }
        }

        List<WriteRequest> writeRequests = uniqueFlights.stream()
                .map(flight -> WriteRequest.builder()
                        .putRequest(PutRequest.builder().item(flight).build())
                        .build())
                        .toList();

        BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
                .requestItems(Map.of(tableName, writeRequests))
                .build();

        dynamoDbClient.batchWriteItem(batchRequest);
    }

    /*Cinco voos mais baratos*/
    public List<Flight> findCheapestFlightByRoute(String route) {

        long now = Instant.now().getEpochSecond();

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("flights")
                .keyConditionExpression("route = :route AND expiryTime > :now")
                .expressionAttributeValues(Map.of(
                        ":route", AttributeValue.fromS(route),
                        ":now", AttributeValue.fromN(String.valueOf(now))
                ))
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        return queryResponse.items().stream()
                .map(this::mapToFlight)
                .sorted(Comparator.comparing(Flight::price))
                .limit(5)
                .collect(Collectors.toList());
    }

    /*Cinco voos mais caros*/
    public List<Flight> findExpensiveFlightByRoute(String route) {

        long now = Instant.now().getEpochSecond();

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("flights")
                .keyConditionExpression("route = :route AND expiryTime > :now")
                .expressionAttributeValues(Map.of(
                        ":route", AttributeValue.fromS(route),
                        ":now", AttributeValue.fromN(String.valueOf(now))
                ))
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        return queryResponse.items().stream()
                .map(this::mapToFlight)
                .sorted(Comparator.comparing(Flight::price).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /*Encontra todos os voos da rota*/
    public List<Flight> findAllValidsFlightsByRoute(String route) {
        long now = Instant.now().getEpochSecond();

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("flights")
                .keyConditionExpression("route = :route AND expiryTime > :now")
                .expressionAttributeValues(Map.of(
                        ":route", AttributeValue.fromS(route),
                        ":now", AttributeValue.fromN(String.valueOf(now))
                ))
                .build();

        return dynamoDbClient.query(queryRequest)
                .items()
                .stream()
                .map(this::mapToFlight)
                .collect(Collectors.toList());
    }

    private Flight mapToFlight(Map<String, AttributeValue> item) {
        return new Flight(
                item.get("route").s(),
                Instant.ofEpochSecond(Long.parseLong(item.get("expiryTime").n())),
                new BigDecimal(item.get("price").n()),
                item.get("currency").s(),
                item.get("airline").s()
        );
    }
}
