package wss.trader;

/**
 * A Strict Trader who is greedy in their dealings.
 * They expect higher returns and offer worse deals.
 */
public class StrictTrader extends Trader {
    
    public StrictTrader() {
        super(TraderType.GREEDY);
    }
} 