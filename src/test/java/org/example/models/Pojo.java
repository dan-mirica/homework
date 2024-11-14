package org.example.models;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Pojo {
    public DownloadIdentifier downloadIdentifier;
    public ArrayList<Opportunity> opportunities;
    public int agency;
    public String deviceType;
    public String country;
    public String city;
    public String listenerId;
}
