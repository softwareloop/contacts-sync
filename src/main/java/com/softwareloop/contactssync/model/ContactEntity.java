package com.softwareloop.contactssync.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.mongojack.ObjectId;

import java.io.Serializable;

@Data
public class ContactEntity implements Serializable {
    @JsonProperty("_id")
    @ObjectId
    String id;
    String userId;
    GooglePerson googlePerson;
}
