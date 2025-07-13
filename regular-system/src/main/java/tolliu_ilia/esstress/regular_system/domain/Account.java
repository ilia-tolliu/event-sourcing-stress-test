package tolliu_ilia.esstress.regular_system.domain;

import java.time.Instant;
import java.util.UUID;

public record Account(
        UUID id,
        Instant createdAt
) {
}
