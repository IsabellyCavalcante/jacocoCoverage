public class MainPartial {

	public static void main(String[] args) {
		try {
			Main3.getOutputTotalAndAddTechniqueFromFile();
		} catch (Exception ex) {
			System.err.println("erro capturado");
			System.out.println();
			System.out.println(ex.getMessage());
		}
	}
}
