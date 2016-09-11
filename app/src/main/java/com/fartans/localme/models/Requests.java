package com.fartans.localme.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by NiRavishankar on 12/12/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Requests {

    @JsonProperty("RequestId")
    public String RequestID;

    @JsonProperty("RequesteUserId")
    public String RequesteUserId;

    @JsonProperty("RequestUserName")
    public String RequestUserName;

    @JsonProperty("RequestMessage")
    public String RequestString;

    @JsonProperty("RequestUserProfession")
    public String RequestUserProfession;

    @JsonProperty("RequestUserProfilePhotoServerPath")
    public String RequestUserProfilePhotoServerPath = "";

    @JsonProperty("RequestedTime")
    public String RequestTime;

    @JsonProperty("RequestImageUrl")
    public String ImagePath;

    @JsonProperty("IsActive")
    public Boolean IsActive;

    public int requestYear;
    public int requestDayOfTheYear;
}
