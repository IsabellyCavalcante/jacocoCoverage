import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
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

/**
 * Versao que dava problema ao colocar em memoria o hashmap todo.
 * 
 * @author Isabelly Cavalcante
 *
 */
public class Main4 {

	private static String sessionName = "";
	private static List<String> sessions = new ArrayList<>();
	private static BufferedWriter bufferedWriter;
	
	private static Map<String, Map<String, Map<Integer, String>>> testes = new HashMap<>();
	private static Map<String, Map<Integer, String>> coverageClasses;

	public static void main(String[] args) throws Exception {
		FileInputStream input = new FileInputStream("dados/jacoco.exec");

		Map<String, ExecutionDataStore> stores = new HashMap<>();

		// Cria o sessionInfo e executionData para o READER do jacoco
		ISessionInfoVisitor sessionInfoVisitor = createSessionInfoVisitor();
		IExecutionDataVisitor executionDataVisitor = createExecutionDataVisitor(stores);

		ExecutionDataReader reader = new ExecutionDataReader(input);
		reader.setSessionInfoVisitor(sessionInfoVisitor);
		reader.setExecutionDataVisitor(executionDataVisitor);
		reader.read();

		// comeca aqui a parte de cobertura

		ICoverageVisitor coverageVisitor = createCoverageVisitor();

		for (Entry<String, ExecutionDataStore> entr : stores.entrySet()) {
			coverageClasses = testes.getOrDefault(entr.getKey(), new HashMap<String, Map<Integer, String>>());
			
			Analyzer analise = new Analyzer(entr.getValue(), coverageVisitor);
			analise.analyzeAll(".", new File("dados"));
			
			testes.put(entr.getKey(), coverageClasses);
			
		}
		
		// total and additional technique
		getOutputTotalAndAddTechnique();

		input.close();
	}

	/**
	 * Retorna o file com a cobertura de cada teste por linha.
	 * @throws IOException
	 */
	private static void getOutputTotalAndAddTechnique() throws IOException {
		bufferedWriter = new BufferedWriter(new FileWriter("output/coverage-v5.txt"));
		
		for (Map<String, Map<Integer, String>> classesPerTest : testes.values()) {
			String covTotal = "";
			for (Map<Integer, String> lines : classesPerTest.values()) {
				for (String cov : lines.values()) {
					covTotal += cov;
				}
			}
			covTotal += "\n";
			writeFile(covTotal, bufferedWriter);
		}
		
		bufferedWriter.close();
	}

	/**
	 * Responsavel pela logica ao verificar cobertura das classes.
	 * @return
	 */
	private static ICoverageVisitor createCoverageVisitor() {
		ICoverageVisitor coverageVisitor = new ICoverageVisitor() {

			@Override
			public void visitCoverage(IClassCoverage c) {
				String className = c.getName();

				Map<Integer, String> coverageLines = coverageClasses.getOrDefault(className, new HashMap<Integer, String>());

				/*
				 * The number of the first line coverage information is
				 * available for. If no line is contained, the method returns
				 * -1. (JACOCO)
				 */
				for (int i = c.getFirstLine(); i <= c.getLastLine(); i++) {
					String coverage = getStatus(c.getLine(i).getStatus());
					//String coverage = String.valueOf(c.getLine(i).getStatus());
					coverageLines.put(i, coverage);
				}
				coverageClasses.put(className, coverageLines);
			}

			/**
			 * Recupera a informacao de cobertura como sendo 0 (se não cobriu -
			 * 0 ou 1 do Jacoco) ou 1 (se cobriu - 2 ou 3 do Jacoco).
			 * 
			 * @param status
			 * @return
			 */
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

	/**
	 * Cria o SessionInfo para o Reader. Seu visitor pega cada sessao de teste
	 * executado.
	 * 
	 * @return
	 */
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

	/**
	 * Cria o ExecutionData para o Reader. Seu visitor popula por teste os seus
	 * respectivos Stores com classes executadas.
	 * 
	 * @param stores
	 * @return
	 */
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
			}
		};
		return executionDataVisitor;
	}

	/**
	 * Escreve o texto recebido no file configurado no buffered.
	 * @param string 
	 * @param bufferedWriter
	 */
	public static void writeFile(String string, BufferedWriter bufferedWriter) {
		try {
			bufferedWriter.write(string);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
