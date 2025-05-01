public abstract class Vision {
	public abstract Path closestFood(Player player, Map map);
	public abstract Path closestWater(Player player, Map map);
	public abstract Path closestGold(Player player, Map map);
	public abstract Path closestTrader(Player player, Map map);
	public abstract Path easiestPath(Player player, Map map);
	public abstract Path secondClosestFood(Player player, Map map);
	public abstract Path secondClosestWater(Player player, Map map);
	public abstract Path secondClosestGold(Player player, Map map);
	public abstract Path secondClosestTrader(Player player, Map map);
}
