import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

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
 * Versao final que captura cobertura no formato das tecnicas de priorizacao.
 * Grava a informacao em um objeto que depois vai para um arquivo e depois le
 * para construir o segundo arquivo no formato necessario para cada tecnica.
 * 
 * @author Isabelly Cavalcante
 *
 */
public class ProcessaCobertura {

	private static String sessionName = "";
	private static List<String> sessions = new ArrayList<>();

	private static Map<String, Map<String, Map<Integer, Boolean>>> testes = new HashMap<>();
	private static Map<String, Map<Integer, Boolean>> coverageClasses;

	/**
	 * Carrega as informacoes do Jacoco.exec e grava tudo em um arquivo.dat que
	 * contem a versao com os objetos.
	 * 
	 * @throws Exception
	 */
	public static void getCoverageFromJacoco() throws Exception {
		FileInputStream input = new FileInputStream("dados/jacoco.exec");

		Map<String, ExecutionDataStore> stores = new HashMap<>();

		System.out.println("----- iniciando leitura do jacoco.exec -----");
		// Cria o sessionInfo e executionData para o READER do jacoco
		ISessionInfoVisitor sessionInfoVisitor = createSessionInfoVisitor();
		IExecutionDataVisitor executionDataVisitor = createExecutionDataVisitor(stores);

		ExecutionDataReader reader = new ExecutionDataReader(input);
		reader.setSessionInfoVisitor(sessionInfoVisitor);
		reader.setExecutionDataVisitor(executionDataVisitor);
		reader.read();

		// comeca aqui a parte de cobertura

		ICoverageVisitor coverageVisitor = createCoverageVisitor();

		FileOutputStream fos = new FileOutputStream("arquivao.dat");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		DeflaterOutputStream dos = new DeflaterOutputStream(bos);
		ObjectOutputStream oos = new ObjectOutputStream(dos);
		oos.writeInt(stores.size());

		System.out.println("----- iniciando leitura de cobertura das classes -----");
		for (Entry<String, ExecutionDataStore> entr : stores.entrySet()) {
			coverageClasses = testes.getOrDefault(entr.getKey(), new HashMap<String, Map<Integer, Boolean>>());

			Analyzer analise = new Analyzer(entr.getValue(), coverageVisitor);
			analise.analyzeAll(".", new File("dados"));

			oos.writeObject(entr.getKey());
			oos.writeObject(coverageClasses);

			oos.reset();
		}

		oos.close();
		input.close();
		System.out.println("----- leitura de cobertura das classes do jacoco finalizada -----");
	}

	/**
	 * Retorna o file com a cobertura de cada teste por linha (utilizada paras as
	 * tecnicas Echalon total e additional).
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void getOutputTotalAndAddEchalonTechniqueFromFile() throws Exception {
		System.out.println("iniciando gravacao no file txt da echalon");
		BufferedWriter bwCoverage = new BufferedWriter(new FileWriter("output/coverage-v2.txt"));
		BufferedWriter bwTests = new BufferedWriter(new FileWriter("output/tests.txt"));

		FileInputStream fis = new FileInputStream("arquivao.dat");
		BufferedInputStream bis = new BufferedInputStream(fis);
		InflaterInputStream iis = new InflaterInputStream(bis);
		ObjectInputStream ois = new ObjectInputStream(iis);

		int contagem = ois.readInt();

		for (int i = 0; i < contagem; i++) {
			StringBuilder testName = new StringBuilder();
			testName.append((String) ois.readObject());

			StringBuilder classesCoverage = new StringBuilder();
			Map<String, Map<Integer, Boolean>> classesPerTest = (Map<String, Map<Integer, Boolean>>) ois.readObject();

			for (String className : classesPerTest.keySet()) {
				Map<Integer, Boolean> classCoverage = classesPerTest.get(className);

				for (Integer line : classCoverage.keySet()) {
					Boolean cov = classCoverage.get(line);
					if (cov) {
						classesCoverage.append(className);
						classesCoverage.append(".");
						classesCoverage.append(line);
						classesCoverage.append(",");
					}
				}
			}

			classesCoverage.append("\n");
			testName.append("\n");
			writeFile(testName.toString(), bwTests);
			writeFile(classesCoverage.toString(), bwCoverage);
		}

		bwCoverage.close();
		bwTests.close();
		System.out.println("final gravacao no file txt");
	}

	/**
	 * Retorna o file com a cobertura de cada teste por linha (utilizada paras as
	 * tecnicas Greedy total e additional).
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void getOutputTotalAndAddTechniqueFromFile() throws IOException, ClassNotFoundException {
		System.out.println("iniciando gravacao no file txt");
		BufferedWriter bwCoverage = new BufferedWriter(new FileWriter("output/coverage-v5.txt"));
		BufferedWriter bwTests = new BufferedWriter(new FileWriter("output/tests.txt"));

		FileInputStream fis = new FileInputStream("arquivao-v1.dat");
		BufferedInputStream bis = new BufferedInputStream(fis);
		InflaterInputStream iis = new InflaterInputStream(bis);
		ObjectInputStream ois = new ObjectInputStream(iis);

		int contagem = ois.readInt();

		for (int i = 0; i < contagem; i++) {
			StringBuilder entry = new StringBuilder();
			entry.append((String) ois.readObject());

			Map<String, Map<Integer, Boolean>> classesPerTest = (Map<String, Map<Integer, Boolean>>) ois.readObject();
			StringBuilder sb = new StringBuilder();

			for (Map<Integer, Boolean> lines : classesPerTest.values()) {
				for (Boolean cov : lines.values()) {
					sb.append(cov ? "1" : "0");
				}
			}

			sb.append("\n");
			entry.append("\n");
			writeFile(entry.toString(), bwTests);
			writeFile(sb.toString(), bwCoverage);
		}

		bwCoverage.close();
		bwTests.close();
		System.out.println("final gravacao no file txt");
	}

	/**
	 * Responsavel pela logica ao verificar cobertura das classes.
	 * 
	 * @return
	 */
	private static ICoverageVisitor createCoverageVisitor() {
		ICoverageVisitor coverageVisitor = new ICoverageVisitor() {

			@Override
			public void visitCoverage(IClassCoverage c) {
				String className = c.getName();

				Map<Integer, Boolean> coverageLines = coverageClasses.getOrDefault(className,
						new HashMap<Integer, Boolean>());

				/*
				 * The number of the first line coverage information is available for. If no
				 * line is contained, the method returns -1. (JACOCO)
				 */
				for (int i = c.getFirstLine(); i <= c.getLastLine(); i++) {
						String coverage = getStatus(c.getLine(i).getStatus());
						coverageLines.put(i, coverage.equals("1"));

				}
				coverageClasses.put(className, coverageLines);
			}

			/**
			 * Recupera a informacao de cobertura como sendo 0 (se não cobriu - 0 ou 1 do
			 * Jacoco) ou 1 (se cobriu - 2 ou 3 do Jacoco).
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
	 * 
	 * @param string
	 * @param bufferedWriter
	 */
	private static void writeFile(String string, BufferedWriter bufferedWriter) {
		try {
			bufferedWriter.write(string);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
