package gartic.models;

import java.util.HashMap;

public class Request {

    private String type;
    private final HashMap<String, String> params;

    public Request() {
        params = new HashMap<>();
    }

    public String type() {
        return type;
    }

    public Request type(String type) {
        this.type = type;
        return this;
    }

    public String param(String key) {
        return params.get(key);
    }

    public Request params(String key, String value) {
        this.params.put(key, value);
        return this;
    }
}
