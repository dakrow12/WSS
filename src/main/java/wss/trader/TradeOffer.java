package wss.trader;

public class TradeOffer {
	// What the offer maker GIVES
	public int offerGold;
	public int offerFood;
	public int offerWater;

	// What the offer maker WANTS to receive
	public int requestGold;
	public int requestFood;
	public int requestWater;

public void setFoodOffered(int val) { this.offerFood = val; }
public void setWaterOffered(int val) { this.offerWater = val; }
public void setGoldOffered(int val) { this.offerGold = val; }

public void setFoodRequested(int val) { this.requestFood = val; }
public void setWaterRequested(int val) { this.requestWater = val; }
public void setGoldRequested(int val) { this.requestGold = val; }

public int getFoodOffered() { return offerFood; }
public int getWaterOffered() { return offerWater; }
public int getGoldOffered() { return offerGold; }
public int getGoldRequested() { return requestGold; }


public boolean hasOffer() {
    return offerFood > 0 || offerWater > 0 || offerGold > 0 ||
           requestFood > 0 || requestWater > 0 || requestGold > 0;
}


public TradeOffer() {
    this(0, 0, 0, 0, 0, 0);
}


	public TradeOffer(int offerGold, int offerFood, int offerWater, int requestGold, int requestFood, int requestWater) {
		this.offerGold = offerGold;
		this.offerFood = offerFood;
		this.offerWater = offerWater;
		this.requestGold = requestGold;
		this.requestFood = requestFood;
		this.requestWater = requestWater;
	}

	// Ensure something is being traded
	public boolean isValid() {
		return (
				offerGold > 0 ||
				offerFood > 0 ||
				offerWater > 0 ||
				requestGold > 0 ||
				requestFood > 0 ||
				requestWater > 0
				);
	}

	@Override
	public String toString() {
		return String.format("Offer(G:%d, F:%d, W:%d) for Request(G:%d, F:%d, W:%d)",
				offerGold, offerFood, offerWater,
				requestGold, requestFood, requestWater);
	}




}
