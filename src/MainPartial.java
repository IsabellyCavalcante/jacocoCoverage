public class MainPartial {

	public static void main(String[] args) {

		try {
			String metodo = args[0];
			String post = args[1];
			
			String arquivao = String.format("arquivao_%s.dat", post);
			
			switch (metodo) {
			case "getCoverage":
				System.out.println("1");
//				ProcessaCobertura.getCoverageFromJacoco(arquivao);
				break;
				
			case "Greedy":
				System.out.println("2");
				System.out.println(post);
//				ProcessaCobertura.getOutputTotalAndAddTechniqueFromFile();
				break;

			case "Echalon":
				System.out.println("3");
//				ProcessaCobertura.getOutputTotalAndAddEchalonTechniqueFromFile();
				break;

			}
		} catch (Exception ex) {
			System.err.println("erro capturado");
			System.out.println();
			System.out.println(ex.getMessage());
		}
	}
}
