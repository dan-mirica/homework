package org.example.models;

import lombok.ToString;

@ToString
public class Opportunity {
    public Object originalEventTime;
    public int maxDuration;
    public Zones zones;
    public PositionUrlSegments positionUrlSegments;
    public int insertionRate;
}
