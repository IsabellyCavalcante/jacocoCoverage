public class MainPartial {

	public static void main(String[] args) {

		try {
			String metodo = args[0];
			String post = args[1];
			
			String arquivao = String.format("arquivao_%s.dat", post);
			
			switch (metodo) {
			case "getCoverage":
				ProcessaCobertura.getCoverageFromJacoco(arquivao);
				break;
				
			case "Greedy":
				ProcessaCobertura.getOutputTotalAndAddTechniqueFromFile(arquivao, post);
				break;

			case "Echalon":
				ProcessaCobertura.getOutputTotalAndAddEchalonTechniqueFromFile(arquivao, post);
				break;

			}
		} catch (Exception ex) {
			System.err.println("erro capturado");
			System.out.println();
			System.out.println(ex.getMessage());
		}
	}
}
