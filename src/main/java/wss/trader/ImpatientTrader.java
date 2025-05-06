package wss.trader;

/**
 * An Impatient Trader who is cautious and has little patience.
 * They will quickly end negotiations if they don't get what they want.
 */
public class ImpatientTrader extends Trader {
    
    public ImpatientTrader() {
        super(TraderType.CAUTIOUS);
    }
} 