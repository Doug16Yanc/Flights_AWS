package douglas.model;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;

@Serdeable
public record Flight(
        String route,
        Instant expiryTime,
        BigDecimal price,
        String currency,
        String airline
) {}