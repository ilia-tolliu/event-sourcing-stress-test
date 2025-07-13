package tolliu.esstress.regular.domain;

import java.time.Instant;
import java.util.UUID;

public record Account(
        UUID id,
        Instant createdAt
) {
}
