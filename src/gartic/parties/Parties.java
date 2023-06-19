package gartic.parties;

import gartic.ClientHandler;

import java.util.HashMap;
import java.util.Map;

public class Parties {

    private static final Map<String, Party> parties = new HashMap<>();

    public static Party getParty(String name) {
        return parties.get(name);
    }

    public static boolean createParty(String name, ClientHandler host) throws RuntimeException {
        if (parties.containsKey(name))
            return false;
        parties.put(name, new Party(name, host));
        return true;
    }


}
