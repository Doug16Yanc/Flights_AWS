package douglas.controller;

import douglas.model.Flight;
import douglas.repository.FlightRepository;
import douglas.utils.DataGenerator;
import io.micronaut.http.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Controller("/flights")
public class FlightController {

    private final FlightRepository flightRepository;
    private final DataGenerator dataGenerator;

    public FlightController(FlightRepository flightRepository, DataGenerator dataGenerator) {
        this.flightRepository = flightRepository;
        this.dataGenerator = dataGenerator;
    }

    @Post("/create-flights")
    public String createFlight() {
        dataGenerator.populate();
        return "DynamoDb populado com sucesso!";
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
