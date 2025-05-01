package wss.game;

import Terrains.Terrain;

import java.util.List;

public class Square {
		private Terrain terrain;
		private List<Item> items;

		public void enterPlayer(Player p) {  }
		public void collectItems(Player p) {  }

		public void setItem(Item item) {
    			this.item = item;
		}
	
		public boolean hasFoodBonus() {
    			return item instanceof FoodBonus;
		}

}

