package com.careerskillroadmap.assessment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RoadmapSelectionRequest(@NotNull @Positive Long careerId) {
}
