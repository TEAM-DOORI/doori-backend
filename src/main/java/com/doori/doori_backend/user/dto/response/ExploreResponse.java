package com.doori.doori_backend.user.dto.response;

import java.util.List;

public record ExploreResponse(
    List<ExploreItem> items,
    String nextCursor,
    boolean hasMore
) {}
