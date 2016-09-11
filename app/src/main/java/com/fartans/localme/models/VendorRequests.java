package com.fartans.localme.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by sattvamedtech on 11/09/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorRequests {

    @JsonProperty("Requests")
    private List<Requests> requests;

    public List<Requests> getRequests() {
        return requests;
    }

    public void setRequests(List<Requests> requests) {
        this.requests = requests;
    }
}
