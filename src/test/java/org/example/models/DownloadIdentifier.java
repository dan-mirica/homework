package org.example.models;

import lombok.ToString;

@ToString
public class DownloadIdentifier {
    public String client;
    public int publisher;
    public String podcastId;
    public String showId;
    public String episodeId;
    public String downloadId;
}