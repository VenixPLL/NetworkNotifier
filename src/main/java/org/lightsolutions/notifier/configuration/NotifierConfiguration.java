package org.lightsolutions.notifier.configuration;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
public class NotifierConfiguration implements Serializable {

    @Since(1.0)
    @SerializedName("endpoints")
    private final String[] pingEndpoints = new String[]{
        "8.8.8.8","8.8.4.4","1.1.1.1","1.0.0.1"
    };


    @Since(1.0)
    @SerializedName("pingInterval")
    private final int pingInterval = 1;



}
