package com.federicoboni.notell.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class UserLabel {
    private static final String EMPTY_STRING = "";
    private static UserLabel userLabel;
    private final HashSet<String> labels;

    private UserLabel(HashSet<String> labels) {
        this.labels = labels;
    }

    public static synchronized UserLabel getInstance() {
        return userLabel == null ? new UserLabel(null) : userLabel;
    }

    public static synchronized void setInstance(List<String> labels) {
        userLabel = new UserLabel(new HashSet<>(labels.stream().filter(s -> !s.trim().equals(EMPTY_STRING)).collect(Collectors.toList())));
    }

    public List<String> getLabels() {
        return labels == null ? new ArrayList<>() : new ArrayList<>(labels);
    }
}
