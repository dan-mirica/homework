package org.example.models;

import lombok.Data;

@Data
public class Opportunity {
    public Long originalEventTime;
    public int maxDuration;
    public Zones zones;
    public PositionUrlSegments positionUrlSegments;
    public int insertionRate;
}
