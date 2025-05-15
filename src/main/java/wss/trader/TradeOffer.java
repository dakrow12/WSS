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
    return isValid();
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
		// At least one thing must be offered and one thing requested
		boolean somethingOffered = offerGold > 0 || offerFood > 0 || offerWater > 0;
		boolean somethingRequested = requestGold > 0 || requestFood > 0 || requestWater > 0;
		return somethingOffered && somethingRequested;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TradeOffer[");
		
		// Offering
		sb.append("Offering: ");
		boolean hasOffering = false;
		if (offerGold > 0) {
			sb.append(offerGold).append(" gold");
			hasOffering = true;
		}
		if (offerFood > 0) {
			if (hasOffering) sb.append(", ");
			sb.append(offerFood).append(" food");
			hasOffering = true;
		}
		if (offerWater > 0) {
			if (hasOffering) sb.append(", ");
			sb.append(offerWater).append(" water");
			hasOffering = true;
		}
		if (!hasOffering) {
			sb.append("nothing");
		}
		
		// Requesting
		sb.append("; Requesting: ");
		boolean hasRequest = false;
		if (requestGold > 0) {
			sb.append(requestGold).append(" gold");
			hasRequest = true;
		}
		if (requestFood > 0) {
			if (hasRequest) sb.append(", ");
			sb.append(requestFood).append(" food");
			hasRequest = true;
		}
		if (requestWater > 0) {
			if (hasRequest) sb.append(", ");
			sb.append(requestWater).append(" water");
			hasRequest = true;
		}
		if (!hasRequest) {
			sb.append("nothing");
		}
		
		sb.append("]");
		return sb.toString();
	}




}
