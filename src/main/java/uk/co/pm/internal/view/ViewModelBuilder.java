package uk.co.pm.internal.view;

import java.util.HashMap;
import java.util.Map;

public class ViewModelBuilder {

    private final Map<String, Object> model;

    private ViewModelBuilder() {
        this.model = new HashMap<>();
    }

    public static ViewModelBuilder init() {
        return new ViewModelBuilder();
    }

    public ViewModelBuilder add(String key, Object value) {
        this.model.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return this.model;
    }
}
