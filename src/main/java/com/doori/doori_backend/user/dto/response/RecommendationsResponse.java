package com.doori.doori_backend.user.dto.response;

import java.util.List;

public record RecommendationsResponse(
    List<RecommendationUserResponse> recommendations
) {}
