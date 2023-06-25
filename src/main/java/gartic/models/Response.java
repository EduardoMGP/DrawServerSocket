package gartic.models;

import java.util.HashMap;

public class Response {

    private boolean success;
    private Code code;
    private String message;
    private final HashMap<String, Object> data;

    public Response() {
        data = new HashMap<>();
    }

    public Code code() {
        return code;
    }

    public Response code(Code code) {
        this.code = code;
        this.message = code.message;
        this.success = code.success;
        return this;
    }

    public boolean isSuccessful() {
        return success;
    }

    public String message() {
        return message;
    }

    public Object data(String key) {
        return data.get(key);
    }

    public Response data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public enum Code {
        DRAWING_RECEIVED(true, "drawing_received", "Drawing received"),
        DRAWN(true, "drawn", "Drawing sent"),
        DRAWN_ERROR(false, "drawn_error", "You are not the host of this party"),

        PARTY_CREATED(true, "party_created", "Party created"),
        PARTY_CONNECTED(true, "connected", "Connected to party"),
        PARTY_DISCONNECTED(true, "disconnected", "Disconnected from party"),
        PARTY_CLOSED(true, "party_closed", "Party closed"),
        PARTY_LIST(true, "party_list", "Party list sent"),

        PARTY_CONNECT_ERROR(false, "connect_error", "Could not find party"),
        PARTY_ALREADY_CONNECTED(false, "connect_already_connected", "You are already connected to this party"),
        PARTY_NOT_CONNECTED(false, "connect_not_connected", "You are not connected to this party"),
        PARTY_DISCONNECT_ERROR(false, "disconnect_error", "You are not connected to this party"),
        PARTY_ERROR(false, "party_error", "Party already exists"),
        PARTY_LIMIT(false, "party_limit", "Party limit reached");

        private final String code;
        private String message;
        private final boolean success;

        Code(boolean success, String code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
        }

        public String code() {
            return code;
        }

        public boolean success() {
            return success;
        }

        public String message() {
            return message;
        }

        public void message(String message) {
            this.message = message;
        }
    }
}
