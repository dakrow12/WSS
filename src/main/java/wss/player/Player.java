package wss.player;

public class Player {
	private int maxStrength, currentStrength;
	private int maxFood, currentFood;
	private int maxWater, currentWater;
	private int currentGold;
	private int positionX, positionY;

	private Vision vision;
	private Brain brain;

	public void move(Direction dir) {  }
	public void rest() {  }
	public void trade(Trader trader) {  }
	public void collectItem(Item item) {  }
}

