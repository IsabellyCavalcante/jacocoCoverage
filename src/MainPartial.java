public class MainPartial {

	public static void main(String[] args) {
		
		try {
			//ProcessaCobertura.teste();
			//ProcessaCobertura.getCoverageFromJacoco();
			ProcessaCobertura.getOutputTotalAndAddEchalonTechniqueFromFile();
			
		} catch (Exception ex) {
			System.err.println("erro capturado");
			System.out.println();
			System.out.println(ex.getMessage());
		}
	}
}
