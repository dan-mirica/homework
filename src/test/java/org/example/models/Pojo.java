package org.example.models;

import lombok.ToString;

import java.util.ArrayList;

@ToString
public class Pojo {
    public DownloadIdentifier downloadIdentifier;
    public ArrayList<Opportunity> opportunities;
    public int agency;
    public String deviceType;
    public String country;
    public String city;
    public String listenerId;
}
