package wss.trader;

/**
 * Represents the different personalities or types of Traders, influencing their negotiation strategy.
 */
public enum TraderType {

    FAIR,           // Balanced negotiation
    GREEDY,         // Demands higher prices, offers lower prices
    CAUTIOUS,       // Reluctant to trade, quits easily
    GENEROUS        // Demands lower prices, offers higher prices

}
