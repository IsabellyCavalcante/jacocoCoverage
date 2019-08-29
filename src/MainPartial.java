public class MainPartial {

	public static void main(String[] args) {

		try {
			String metodo = args[0];
			String post = args[1];
			
			String arquivao = String.format("../output/arquivao_%s.dat", post);
			
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
			System.out.println();
			System.err.println("Algo deu errado. Erro capturado:");
			System.out.println(ex.getMessage());
		}
	}
}
