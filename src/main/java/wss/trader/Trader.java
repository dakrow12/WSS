package wss.trader;

import wss.items.Item;
import wss.player.Player;

import java.util.Random; // Needed for some randomness

/**
 * Represents a Trader NPC that the player can interact with to exchange resources.
 * Inherits from Item, so it can be placed on the map.
 */
public class Trader extends Item {
	private TraderType personality;
	private TraderState currentState;
	private int negotiationRound;
	private TradeOffer lastTraderOffer; // To store the trader's counter-offer

	// Define base values for items (can adjust)
	private static final int VALUE_FOOD = 1;
	private static final int VALUE_WATER = 2;
	private static final int VALUE_GOLD = 5;

	private static final Random random = new Random(); // For slight variations

	public Trader(TraderType personality) {
		super(true); // Traders are always repeating
		this.personality = personality;
		this.reset();
	}

	
	public void reset() {
		this.currentState = TraderState.IDLE;
		this.negotiationRound = 0;
		this.lastTraderOffer = null;
		// System.out.println("Trader (" + personality + ") reset to IDLE.");
	}

	/**
	 * Called when the player interacts with the trader's square.
	 * Initiates the negotiation process if the trader is idle.
	 */
	
	public void activate(Player player) {
		if (this.currentState == TraderState.IDLE) {
			System.out.println("Player encounters a " + this.personality + " Trader.");
			this.currentState = TraderState.NEGOTIATING;
			this.negotiationRound = 0; // Reset rounds at start of interaction
			System.out.println("Trader is now NEGOTIATING. Waiting for player offer.");
			// The game loop/Brain should now prompt the player/AI to make an offer
		} else if (this.currentState == TraderState.RAN_OUT_OF_PATIENCE) {
			System.out.println("Trader (" + this.personality + ") refuses to negotiate further right now.");
		} else if (this.currentState == TraderState.TRADE_ACCEPTED) {
			System.out.println("Trader (" + this.personality + ") just completed a trade. Resting.");
			reset(); // Reset after successful trade to allow new interaction later
		}
		else {
			System.out.println("Trader (" + this.personality + ") is currently busy (" + this.currentState + ").");
		}
	}

	/**
	 * Evaluates an offer made by the player.
	 * Changes state based on evaluation (ACCEPTED, REJECTED, RAN_OUT_OF_PATIENCE).
	 * Does NOT generate a counter-offer here.
	 * @param playerOffer The offer made by the player.
	 * @return true if the offer was accepted, false otherwise.
	 */
	public boolean evaluatePlayerOffer(TradeOffer playerOffer) {
		if (currentState != TraderState.NEGOTIATING && currentState != TraderState.WAITING_FOR_PLAYER_RESPONSE) {
			System.out.println("Trader cannot evaluate offer in state: " + currentState);
			return false; // Can only evaluate when expecting an offer
		}
		if (!playerOffer.isValid()) {
			System.out.println("Trader ignores invalid offer.");
			this.currentState = TraderState.TRADE_REJECTED; // Reject invalid offers
			return false;
		}

		System.out.println("Trader (" + personality + ") evaluating player offer: " + playerOffer);
		this.currentState = TraderState.EVALUATING_PLAYER_OFFER;
		this.negotiationRound++;

		// Check patience
		if (checkPatience()) {
			return false; // Trader quit
		}

		boolean accepted = switch (this.personality) {
            case FAIR -> evaluateFair(playerOffer);
            case GREEDY -> evaluateGreedy(playerOffer);
            case CAUTIOUS -> evaluateCautious(playerOffer);
			case GENEROUS -> evaluateGenerous(playerOffer);
            default -> false;
        };

        if (accepted) {
			this.currentState = TraderState.TRADE_ACCEPTED;
			// The Player side should call Player.finalizeTrade(playerOffer)
			System.out.println("Trader (" + personality + ") ACCEPTS player offer.");
		} else {
			// If not accepted, maybe generate a counter or just reject
			// For simplicity now, just reject. Counter logic is separate.
			this.currentState = TraderState.TRADE_REJECTED;
			System.out.println("Trader (" + personality + ") REJECTS player offer.");
			// The game loop/Brain could now call generateCounterOffer()
		}
		return accepted;
	}


	/**
	 * Generates a counter-offer based on the player's last rejected offer.
	 * Should only be called after evaluatePlayerOffer returned false.
	 * @param playerOffer The player's offer that was just rejected.
	 * @return A new TradeOffer from the trader, or null if no counter is made.
	 */
	public TradeOffer generateCounterOffer(TradeOffer playerOffer) {
		// Can only counter if player's offer was just rejected and trader didn't quit
		if (currentState != TraderState.TRADE_REJECTED) {
			System.out.println("Trader cannot counter offer in state: " + currentState);
			return null;
		}
		// Don't counter if already impatient
		if (checkPatience()) {
			return null;
		}


		System.out.println("Trader (" + personality + ") considering counter to player offer: " + playerOffer);
		TradeOffer counter = switch (this.personality) {
            case FAIR -> generateFairCounter(playerOffer);
            case GREEDY -> generateGreedyCounter(playerOffer);
            case CAUTIOUS -> generateCautiousCounter(playerOffer);
			case GENEROUS -> generateGenerousCounter(playerOffer);
            default -> null;
        };

        if (counter != null && counter.isValid()) {
			System.out.println("Trader (" + personality + ") proposes counter-offer: " + counter);
			this.currentState = TraderState.WAITING_FOR_PLAYER_RESPONSE;
			this.lastTraderOffer = counter; // Store the offer
		} else {
			System.out.println("Trader (" + personality + ") decides not to make a counter-offer.");
			// State remains TRADE_REJECTED or RAN_OUT_OF_PATIENCE
			reset(); // End negotiation if no counter generated after rejection
		}
		return counter;
	}

	/**
	 * Allows the player to accept the trader's last counter-offer.
	 * @return true if the acceptance is valid and state is updated, false otherwise.
	 */
	public boolean acceptTraderCounterOffer() {
		if (currentState == TraderState.WAITING_FOR_PLAYER_RESPONSE && lastTraderOffer != null) {
			System.out.println("Player accepts Trader's counter offer: " + lastTraderOffer);
			this.currentState = TraderState.TRADE_ACCEPTED;
			// The Player side should call Player.finalizeTrade(lastTraderOffer)
			return true;
		}
		System.out.println("Cannot accept trader offer now (State: " + currentState + ", Offer: " + lastTraderOffer + ")");
		return false;
	}

	/**
	 * Allows the player to reject the trader's last counter-offer.
	 */
	public void rejectTraderCounterOffer() {
		if (currentState == TraderState.WAITING_FOR_PLAYER_RESPONSE) {
			System.out.println("Player rejects Trader's counter offer.");
			this.currentState = TraderState.TRADE_REJECTED;
			// Negotiation might end here, or player could make a new offer. Resetting for simplicity.
			reset();
		}
	}


	// --- Patience Check ---
	private boolean checkPatience() {
		int patienceLimit = switch (this.personality) {
            case FAIR -> 5;
            case GREEDY -> 4;
            case CAUTIOUS -> 2;
			case GENEROUS -> 7;
            default -> 3;
        };

        if (negotiationRound > patienceLimit) {
			System.out.println("Trader (" + personality + ") ran out of patience after " + negotiationRound + " rounds!");
			this.currentState = TraderState.RAN_OUT_OF_PATIENCE;
			return true; // Impatient
		}
		return false; // Still patient
	}

	// --- Evaluation Logic ---
	private int calculateOfferValue(int gold, int food, int water) {
		return (gold * VALUE_GOLD) + (food * VALUE_FOOD) + (water * VALUE_WATER);
	}

	// Player offer = what player GIVES
	// Player request = what player WANTS
	private boolean evaluateFair(TradeOffer offer) {
		int valueGivenByPlayer = calculateOfferValue(offer.offerGold, offer.offerFood, offer.offerWater);
		int valueReceivedByPlayer = calculateOfferValue(offer.requestGold, offer.requestFood, offer.requestWater);
		// Fair trader wants roughly equal value (e.g., player gives >= 90% of what they receive)
		return valueGivenByPlayer >= valueReceivedByPlayer * 0.9;
	}

	private boolean evaluateGreedy(TradeOffer offer) {
		int valueGivenByPlayer = calculateOfferValue(offer.offerGold, offer.offerFood, offer.offerWater);
		int valueReceivedByPlayer = calculateOfferValue(offer.requestGold, offer.requestFood, offer.requestWater);
		// Greedy trader wants much more than they give (e.g., player gives >= 150% of what they receive)
		return valueGivenByPlayer >= valueReceivedByPlayer * 1.5;
	}

	private boolean evaluateGenerous(TradeOffer offer) {
		int valueGivenByPlayer = calculateOfferValue(offer.offerGold, offer.offerFood, offer.offerWater);
		int valueReceivedByPlayer = calculateOfferValue(offer.requestGold, offer.requestFood, offer.requestWater);
		// Generous trader wants much less than they give (e.g., player gives >= 50% of what they receive)
		return valueGivenByPlayer >= valueReceivedByPlayer * 0.5;
	}
	private boolean evaluateCautious(TradeOffer offer) {
		int valueGivenByPlayer = calculateOfferValue(offer.offerGold, offer.offerFood, offer.offerWater);
		int valueReceivedByPlayer = calculateOfferValue(offer.requestGold, offer.requestFood, offer.requestWater);
		// Cautious trader accepts only slightly favorable trades and small amounts
		boolean favorable = valueGivenByPlayer >= valueReceivedByPlayer * 1.1;
		boolean smallTrade = (offer.offerGold + offer.requestGold <= 2) &&
				(offer.offerFood + offer.requestFood <= 5) &&
				(offer.offerWater + offer.requestWater <= 5);
		return favorable && smallTrade;
	}

	// --- Counter-Offer Generation Logic (Basic Examples) ---

	// Try to make the trade slightly better for the trader than the player's offer
	private TradeOffer generateFairCounter(TradeOffer rejectedPlayerOffer) {
		// Example: Ask for 10% more value, or offer 10% less value.
		int valuePlayerOffered = calculateOfferValue(rejectedPlayerOffer.offerGold, rejectedPlayerOffer.offerFood, rejectedPlayerOffer.offerWater);
		int valuePlayerRequested = calculateOfferValue(rejectedPlayerOffer.requestGold, rejectedPlayerOffer.requestFood, rejectedPlayerOffer.requestWater);

		// Simple counter: Trader requests the same items, but offers slightly less back.
		int slightlyLessFood = Math.max(0, rejectedPlayerOffer.requestFood - 1);
		int slightlyLessWater = Math.max(0, rejectedPlayerOffer.requestWater - 1);
		// Only counter if it's different from player's original request
		if(slightlyLessFood != rejectedPlayerOffer.requestFood || slightlyLessWater != rejectedPlayerOffer.requestWater) {
			return new TradeOffer(rejectedPlayerOffer.requestGold, slightlyLessFood, slightlyLessWater, // What trader offers (player requests)
					rejectedPlayerOffer.offerGold, rejectedPlayerOffer.offerFood, rejectedPlayerOffer.offerWater); // What trader requests (player offers)
		}
		return null; // Can't make a simple counter
	}

	// Try to make the trade slightly better for the player than the trader's offer
	private TradeOffer generateGenerousCounter(TradeOffer rejectedPlayerOffer) {
		// Example: Ask for 10% less value, or offer 10% more value.
		int valuePlayerOffered = calculateOfferValue(rejectedPlayerOffer.offerGold, rejectedPlayerOffer.offerFood, rejectedPlayerOffer.offerWater);
		int valuePlayerRequested = calculateOfferValue(rejectedPlayerOffer.requestGold, rejectedPlayerOffer.requestFood, rejectedPlayerOffer.requestWater);

		// Simple counter: Trader requests the same items, but offers a lot less back.
		int slightlyLessFood = Math.max(0, rejectedPlayerOffer.requestFood - 2);
		int slightlyLessWater = Math.max(0, rejectedPlayerOffer.requestWater - 2);
		// Only counter if it's different from player's original request
		if(slightlyLessFood != rejectedPlayerOffer.requestFood || slightlyLessWater != rejectedPlayerOffer.requestWater) {
			return new TradeOffer(rejectedPlayerOffer.requestGold, slightlyLessFood, slightlyLessWater, // What trader offers (player requests)
					rejectedPlayerOffer.offerGold, rejectedPlayerOffer.offerFood, rejectedPlayerOffer.offerWater); // What trader requests (player offers)
		}
		return null; // Can't make a simple counter
	}

	// Try to make the trade significantly better for the trader
	private TradeOffer generateGreedyCounter(TradeOffer rejectedPlayerOffer) {
		// Example: Ask for 50% more value, or offer 50% less value.
		int slightlyLessFood = Math.max(0, (int)(rejectedPlayerOffer.requestFood * 0.5)); // Offer only half the food
		int slightlyLessWater = Math.max(0, (int)(rejectedPlayerOffer.requestWater * 0.5)); // Offer only half the water

		if(slightlyLessFood != rejectedPlayerOffer.requestFood || slightlyLessWater != rejectedPlayerOffer.requestWater) {
			return new TradeOffer(rejectedPlayerOffer.requestGold / 2, slightlyLessFood, slightlyLessWater, // What trader offers (halved)
					rejectedPlayerOffer.offerGold * 2, rejectedPlayerOffer.offerFood, rejectedPlayerOffer.offerWater); // What trader requests (doubled gold request)
		}
		return null;
	}

	// Cautious traders rarely counter-offer, prefer small adjustments if they do.
	private TradeOffer generateCautiousCounter(TradeOffer rejectedPlayerOffer) {
		// Only counter if the original offer was already small
		boolean smallTrade = (rejectedPlayerOffer.offerGold + rejectedPlayerOffer.requestGold <= 2) &&
				(rejectedPlayerOffer.offerFood + rejectedPlayerOffer.requestFood <= 5) &&
				(rejectedPlayerOffer.offerWater + rejectedPlayerOffer.requestWater <= 5);
		if (smallTrade && random.nextBoolean()) { // Only counter 50% of the time
			// Make a tiny adjustment
			int lessFood = Math.max(0, rejectedPlayerOffer.requestFood - 1);
			if(lessFood != rejectedPlayerOffer.requestFood) {
				return new TradeOffer(rejectedPlayerOffer.requestGold, lessFood, rejectedPlayerOffer.requestWater,
						rejectedPlayerOffer.offerGold, rejectedPlayerOffer.offerFood, rejectedPlayerOffer.offerWater);
			}
		}
		return null; // Often doesn't counter
	}


public TradeResponse makeTrade(TradeOffer offer) {
    boolean accepted = evaluatePlayerOffer(offer);
    return new TradeResponse(accepted, accepted ? offer : null);
}


	// --- Getters ---
	public TraderState getCurrentState() {
		return currentState;
	}

	public TraderType getPersonality() {
		return personality;
	}

	public TradeOffer getLastTraderOffer() {
		return lastTraderOffer;
	}
}