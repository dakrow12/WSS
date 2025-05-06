package wss.trader;

public class TradeResponse {
    private final boolean accepted;
    private final TradeOffer finalOffer;

    public TradeResponse(boolean accepted, TradeOffer finalOffer) {
        this.accepted = accepted;
        this.finalOffer = finalOffer;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public TradeOffer getFinalOffer() {
        return finalOffer;
    }
}
