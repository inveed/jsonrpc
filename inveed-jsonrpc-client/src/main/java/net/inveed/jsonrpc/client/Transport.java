package net.inveed.jsonrpc.client;

import java.io.IOException;

/**
 * Abstract transport for JSON-RPC communication
 */
public interface Transport {

    /**
     * Passes a JSON-RPC request in a text form to a backend and
     * returns a JSON-RPC response in a text form as well
     *
     * @param request JSON-RPC request as a string
     * @return JSON-RPC response as a string
     * @throws IOException when cannot send request
     */
    
    public String pass( String request) throws IOException;
}
