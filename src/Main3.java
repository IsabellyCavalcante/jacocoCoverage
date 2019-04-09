import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

public class Main3 {

	private static String sessionName = "";
	private static List<String> sessions = new ArrayList<>();
	private static BufferedWriter bufferedWriter;

	private static Map<String, Map<Integer, String>> coverageClasses = new HashMap<>();

	public static void main(String[] args) throws Exception {
		// Inicializa um arquivo e ler o exec para um stream
		bufferedWriter = new BufferedWriter(new FileWriter("execucao.txt"));
		FileInputStream input = new FileInputStream("dados/jacoco.exec");

		Map<String, ExecutionDataStore> stores = new HashMap<>();

		// Cria o sessionInfo e executionData para o READER do jacoco
		ISessionInfoVisitor sessionInfoVisitor = createSessionInfoVisitor();
		IExecutionDataVisitor executionDataVisitor = createExecutionDataVisitor(stores);

		ExecutionDataReader reader = new ExecutionDataReader(input);
		reader.setSessionInfoVisitor(sessionInfoVisitor);
		reader.setExecutionDataVisitor(executionDataVisitor);
		reader.read();

		writeFile("fim", bufferedWriter);
		bufferedWriter.close();

		// começa aqui a parte de cobertura

		ICoverageVisitor coverageVisitor = createCoverageVisitor();

		bufferedWriter = new BufferedWriter(new FileWriter("coverage-v3.txt"));

		for (Entry<String, ExecutionDataStore> entr : stores.entrySet()) {
			Analyzer analise = new Analyzer(entr.getValue(), coverageVisitor);
			analise.analyzeAll(".", new File("dados"));
			// String linha = String.format("%s \n", tmp);
			// writeFile(linha, bufferedWriter);
		}

		bufferedWriter.close();
		input.close();
	}

	private static ICoverageVisitor createCoverageVisitor() {
		ICoverageVisitor coverageVisitor = new ICoverageVisitor() {

			@Override
			public void visitCoverage(IClassCoverage c) {
				String className = c.getName();

				Map<Integer, String> coverageLines = coverageClasses.getOrDefault(className, new HashMap<Integer, String>());

				for (int i = c.getFirstLine(); i <= c.getLastLine(); i++) {
					String coverage = getStatus(c.getLine(i).getStatus());
					coverageLines.put(i, coverage);
				}
				
				coverageClasses.put(className, coverageLines);

			}

			
			private String getStatus(int status) {
				switch (status) {
				case ICounter.NOT_COVERED:
					return "0";
				case ICounter.PARTLY_COVERED:
					return "1";
				case ICounter.FULLY_COVERED:
					return "1";
				}
				return "0";
			}
		};
		return coverageVisitor;
	}

	// Cria o SessionInfo para o Reader. Seu visitor pega cada sessao de teste
	// executado
	private static ISessionInfoVisitor createSessionInfoVisitor() {
		ISessionInfoVisitor sessionInfoVisitor = new ISessionInfoVisitor() {
			@Override
			public void visitSessionInfo(SessionInfo info) {
				sessionName = info.getId();
				sessions.add(sessionName);
			}
		};
		return sessionInfoVisitor;
	}

	// Cria o ExecutionData para o Reader. Seu visitor popula por teste os seus
	// respectivos Stores com classes executadas.
	private static IExecutionDataVisitor createExecutionDataVisitor(Map<String, ExecutionDataStore> stores) {
		IExecutionDataVisitor executionDataVisitor = new IExecutionDataVisitor() {
			@Override
			public void visitClassExecution(ExecutionData executionData) {
				if (!executionData.getName().startsWith("br/gov/dpf/epol")) {
					return;
				}
				ExecutionDataStore store = stores.getOrDefault(sessionName, new ExecutionDataStore());
				store.put(executionData);
				stores.put(sessionName, store);

				String saida = String.format("Session: %s -> executionData: %s \n", sessionName,
						executionData.getName());
				writeFile(saida, bufferedWriter);
			}
		};
		return executionDataVisitor;
	}

	public static void writeFile(String string, BufferedWriter bufferedWriter) {
		try {
			bufferedWriter.write(string);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
