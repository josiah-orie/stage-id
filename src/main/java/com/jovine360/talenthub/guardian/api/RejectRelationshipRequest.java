package com.jovine360.talenthub.guardian.api;

import jakarta.validation.constraints.Size;

public record RejectRelationshipRequest(@Size(max = 500) String reason) {}
