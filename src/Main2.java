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

public class Main2 {

	private static String sessionName = "";
	private static List<String> sessions = new ArrayList<>();
	private static Map<String, Integer> totalClassesPorSession = new HashMap<>();
	private static Map<String, Integer> qtdValidos = new HashMap<>();
	private static BufferedWriter bufferedWriter;
	private static String tmp = "";

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

		// gravando no arquivo info de classes executadas
		for (String session : totalClassesPorSession.keySet()) {
			writeFile(String.format("total de classes executadas da session %s: %s \n", session,
					totalClassesPorSession.get(session)), bufferedWriter);
			writeFile(String.format("total de classes sem ser testes executadas da session %s: %s \n", session,
					qtdValidos.get(session)), bufferedWriter);
		}
		writeFile("fim", bufferedWriter);
		bufferedWriter.close();

		// gravando lista de sessions
		bufferedWriter = new BufferedWriter(new FileWriter("sessions.txt"));
		writeFile(String.format("total na lista de sessions: %s \n", sessions.toString()), bufferedWriter);
		bufferedWriter.close();

		// começa aqui a parte de cobertura

		// criando file de testes
		bufferedWriter = new BufferedWriter(new FileWriter("testes.txt"));
		for (Entry<String, ExecutionDataStore> entr : stores.entrySet()) {
			String linha = String.format("%s \n", entr.getKey());
			writeFile(linha, bufferedWriter);
		}

		bufferedWriter.close();

		// criando file de cobertura

		// Cria o cara da cobertura
		ICoverageVisitor coverageVisitor = createCoverageVisitor();

		bufferedWriter = new BufferedWriter(new FileWriter("coverage.txt"));
		//TODO tirar o teste sem nada
		for (Entry<String, ExecutionDataStore> entr : stores.entrySet()) {
			tmp = "";
			Analyzer analise = new Analyzer(entr.getValue(), coverageVisitor);
			analise.analyzeAll(".", new File("dados"));
			String linha = String.format("%s \n", tmp);
			writeFile(linha, bufferedWriter);
		}

		bufferedWriter.close();
		input.close();
	}

	private static ICoverageVisitor createCoverageVisitor() {
		ICoverageVisitor coverageVisitor = new ICoverageVisitor() {

			@Override
			public void visitCoverage(IClassCoverage c) {
				String saida = "";

				for (int i = c.getFirstLine(); i <= c.getLastLine(); i++) {
					// a linha abaixo eh para pegar o status como quando vem do jacoco 
					// 0 - javadoc e etc 
					// 1 - not cov 
					// 2 - full cov 
					// 3 - part cov
					
//					 saida += c.getLine(i).getStatus();

					int status = c.getLine(i).getStatus();
					switch (status) {
						case ICounter.NOT_COVERED:
							saida += "0";
							break;
						case ICounter.PARTLY_COVERED:
							saida += "1";
							break;
						case ICounter.FULLY_COVERED:
							saida += "1";
							break;
						default:
							saida += "0";
							break;
					}
				}
				tmp = tmp + saida;
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
				extractInfo(executionData);
			}

			// metodo que conta o numero de classes executadas por sessao total
			// e classes sem ser de testes
			private void extractInfo(ExecutionData executionData) {
				int count = totalClassesPorSession.getOrDefault(sessionName, 0);
				count += 1;
				totalClassesPorSession.put(sessionName, count);
				if (!executionData.getName().contains("Test")) {
					int countValidos = qtdValidos.getOrDefault(sessionName, 0);
					countValidos += 1;
					qtdValidos.put(sessionName, countValidos);
				}
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
