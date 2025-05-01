package wss;

public class Main {
	public static void main(String[] args) {
		System.out.println("Hello world!");

		//just a dummy code for testing
		Player p = new Player(10, 10, 10, new Cautious(), new ConservativeBrain());
		System.out.println("Position: " + p.getX() + ", " + p.getY());
		p.rest();

	}
}
