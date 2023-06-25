package gartic.models;

import java.util.HashMap;

public class Request {

    private Action action;
    private final HashMap<String, Object> data;

    public Request() {
        data = new HashMap<>();
    }

    public void action(String action) {
        this.action = Action.valueOf(action.toUpperCase());
    }

    public Action action() {
        System.out.println("action: " + action);
        return action;
    }

    public Object data(String key) {
        return data.get(key);
    }

    public enum Action {

        PARTY_CREATE,
        PARTY_CONNECT,
        PARTY_DISCONNECT,
        PARTY_LIST,
        DRAWING;
        Action() {
        }
    }
}
