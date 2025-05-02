package wss.trader;

/**
 * Represents the possible states of a Trader during negotiation.
 */
public enum TraderState {

    IDLE,                               // Waiting for player interaction
    NEGOTIATING,                        // Actively trading with player
    WAITING_FOR_PLAYER_RESPONSE,        // Trader made offer, waiting for response
    EVALUATING_PLAYER_OFFER,            // Trader is processing player offer
    TRADE_ACCEPTED,                     // Trader accepts player's offer
    TRADE_REJECTED,                     // Trader rejects player's offer
    RAN_OUT_OF_PATIENCE                 // Quit after too many counters

}
