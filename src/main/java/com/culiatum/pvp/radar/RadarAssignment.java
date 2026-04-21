package com.culiatum.pvp.radar;

import java.util.UUID;

public record RadarAssignment(UUID targetUuid, String targetName, long expiresAt, String label) {
}
