public class Trader extends Item {
	private TraderType personality;
	private TraderState state;

	public boolean accept(TradeOffer offer) {
		return false;
	}
	public TradeOffer counterOffer(TradeOffer offer) {
		return null;
	}
	public void reset() {

	}

	@Override
	public void activate(Player player) {

	}
}
