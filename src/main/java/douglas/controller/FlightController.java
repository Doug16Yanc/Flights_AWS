package douglas.controller;

import douglas.model.Flight;
import douglas.repository.FlightRepository;
import douglas.utils.PopulateDynamoDb;
import io.micronaut.http.annotation.*;

import java.util.List;

@Controller("/flights")
public class FlightController {

    private final FlightRepository flightRepository;
    private final PopulateDynamoDb populateDynamoDb;

    public FlightController(FlightRepository flightRepository, PopulateDynamoDb populateDynamoDb) {
        this.flightRepository = flightRepository;
        this.populateDynamoDb = populateDynamoDb;
    }

    @Post("/create-flights")
    public String createFlight() {
        populateDynamoDb.populate();
        return "DynamoDb populado com sucesso";
    }

    @Get("/cheapest/{route}")
    public List<Flight> getCheapestFlight(@PathVariable String route) {
        return flightRepository.findCheapestFlightByRoute(route);
    }

    @Get("/expensive/{route}")
    public List<Flight> getExpensiveFlight(@PathVariable String route) {
        return flightRepository.findExpensiveFlightByRoute(route);
    }

    @Get("/all/{route}")
    public List<Flight> getAllFlights(@PathVariable String route) {
        return flightRepository.findAllValidsFlightsByRoute(route);
    }
}
