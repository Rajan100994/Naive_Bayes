import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class NaiveBayes {

	static BigDecimal trulyIdentified = BigDecimal.ZERO;
	static BigDecimal notTrulyIdentified = BigDecimal.ZERO;
	
	static BigDecimal posteriorCount = BigDecimal.ZERO;
	
	public static void main(String[] args) {
		
		
		//System.out.println("Naive Bayes ");
		HashMap<String,HashMap<String,Integer>> categoryMap = new HashMap<String,HashMap<String,Integer>>();
		HashMap<String,BigDecimal> prior = new HashMap<String,BigDecimal>();
		HashMap<String,BigDecimal> likelyHood = new HashMap<String,BigDecimal>();
		
		try
		{
			
			HashMap<String,BigDecimal> totalSamples = new HashMap<String,BigDecimal>();
			
			
			//getting Training File and generating Frequency map 
			traverseDir(new File(args[0]), categoryMap,totalSamples,args[0]);
			
			//Calculate Prior 
			calculatePrior(totalSamples,prior,categoryMap);
			
			//Calculate Likely hood for test Data  
			calculateLikelyHood(new File(args[1]),totalSamples,likelyHood,categoryMap,prior);
			
			//System.out.println(categoryMap);
			//System.out.println(categoryMap.get("alt.atheism"));
			//System.out.println("trulyIdentified : "+trulyIdentified);
			//System.out.println("notTrulyIdentified : "+notTrulyIdentified);
			
			System.out.println("Accuracy : "+(trulyIdentified.divide((trulyIdentified.add(notTrulyIdentified)),50, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)));
			
			//System.out.println("posteriorCount : "+posteriorCount);
		}
		catch(FileNotFoundException fe)
		{
			System.out.println("Please Verify Path for Training and Test Files");
		}
		catch(Exception e)
		{
			System.out.println("Usage : NaiveBayes <Training Data Set> <Testing Data Set>");
			e.printStackTrace();
		}
		
		
	}
	
	private static void calculateLikelyHood(File node, HashMap<String, BigDecimal> totalSamples,
			HashMap<String, BigDecimal> likelyHood, HashMap<String,HashMap<String,Integer>> categoryMap, HashMap<String,BigDecimal> prior) 
	{
		
		if(node.isDirectory()){
			
			String[] subNote = node.list();
			
			for(String filename : subNote){
				calculateLikelyHood(new File(node, filename),totalSamples,likelyHood,categoryMap,prior);
			}
		}
		else
		{
			//It is File so Process This File 
			//System.out.println("File Scanned : "+node.getName());
			//Its Original Category
			String orgCategory = node.getParentFile().getName();
			
			//Its Predicted Category
			String predCategory = "";
			BigDecimal maxPosterior = BigDecimal.ZERO;
			
			//Calculate Total File Count 
			
			HashMap<String,Integer> testWordMap = new HashMap<String,Integer>();
			createWordMap(testWordMap,node);
			
			//Posterior 
			HashMap<String,BigDecimal> posterior = new HashMap<String,BigDecimal>();
			
			
			//Find Likely hood of Every Word
			for(Map.Entry<String,HashMap<String,Integer>> categoryEntry : categoryMap.entrySet())
			{
				//System.out.println(categoryEntry.getKey());
				
				BigDecimal totalSamplesVal = totalSamples.get(categoryEntry.getKey());
				
				BigDecimal totalMul = BigDecimal.ONE;
				
				for(Map.Entry<String,Integer> entry : testWordMap.entrySet()){
					
					if(!"".equals(entry.getKey()))
					{
						double freq = (categoryEntry.getValue().get(entry.getKey())!=null ? categoryEntry.getValue().get(entry.getKey()) : 0);
						/*
						if(freq==1){
							totalMul = totalMul.multiply(new BigDecimal(freq).divide(totalSamplesVal.add(new BigDecimal(categoryMap.size())),50, RoundingMode.HALF_UP));
						}
						else{
							totalMul = totalMul.multiply(new BigDecimal(freq).divide(totalSamplesVal,50, RoundingMode.HALF_UP));
						}*/
						totalMul = totalMul.multiply(new BigDecimal(freq+1).divide(totalSamplesVal.add(new BigDecimal(categoryMap.size())),50, RoundingMode.HALF_UP));

							
					}
				}
				
				totalMul = totalMul.multiply(prior.get(categoryEntry.getKey()));
				
				//System.out.println("posterior is : "+totalMul+" For category : "+ categoryEntry.getKey());
				
				if(totalMul.compareTo(maxPosterior)>0)
				{
					maxPosterior = totalMul;
					predCategory = categoryEntry.getKey();
				}
				
				if(totalMul.compareTo(BigDecimal.ZERO)==0)
					posteriorCount = posteriorCount.add(BigDecimal.ONE);
				
				posterior.put(categoryEntry.getKey(), totalMul);
			}
			
			
			
			testWordMap = null;
			
			//Checking Probability 
			if(orgCategory.equals(predCategory))
			{
				//System.out.println("Truly Identified : "+node.getParentFile().getName() + " : "+node.getName());
				trulyIdentified = trulyIdentified.add(BigDecimal.ONE);
			}
			else{
				notTrulyIdentified = notTrulyIdentified.add(BigDecimal.ONE);
			}
		}
	}

	private static void calculatePrior(HashMap<String, BigDecimal> totalSamples,HashMap<String, BigDecimal> prior,
			HashMap<String, HashMap<String, Integer>> categoryMap) {

		BigDecimal totalInstances = BigDecimal.ZERO;
		
		//System.out.println(prior);
		
		//Get Total Sum of Items
		for(Map.Entry<String, BigDecimal> entry : totalSamples.entrySet())
		{
			totalInstances = totalInstances.add(entry.getValue());
		}
		
		//Calculate Prior
		for(Map.Entry<String, BigDecimal> entry : totalSamples.entrySet())
		{
			prior.put(entry.getKey(), entry.getValue().divide(totalInstances,50, RoundingMode.HALF_UP)); ;
		}
		
		//System.out.println(prior);
	}

	public static void traverseDir(File node,HashMap<String,HashMap<String,Integer>> categoryMap,
			HashMap<String,BigDecimal> prior, String fileName) throws FileNotFoundException{

		//System.out.println(node.getAbsoluteFile());

		if(node.isDirectory()){
			
			
			if(!node.getAbsolutePath().equals(fileName.replaceAll("\\\\\\\\",  Matcher.quoteReplacement("\\"))))
			categoryMap.put(node.getName(), new HashMap<String,Integer>());
			
			String[] subNote = node.list();
			
			for(String filename : subNote){
				traverseDir(new File(node, filename),categoryMap,prior,fileName);
			}
		}
		else
		{
			//It is File so Process This File 
			//System.out.println(node.getParentFile().getName());
			
			//Calculate Total File Count 
			prior.put(node.getParentFile().getName(), new BigDecimal(node.getParentFile().list().length));
			
			HashMap<String,Integer> wordMap = categoryMap.get(node.getParentFile().getName());
			createWordMap(wordMap,node);
			categoryMap.put(node.getParentFile().getName(), wordMap);
			wordMap = null;
		}
	}

	private static void createWordMap(HashMap<String, Integer> wordMap, File node) {
		
		
		//Read File and Get All words
		
		BufferedReader br = null;
		FileReader fr = null;
		boolean startFlag = false;
		try {

			fr = new FileReader(node);
			br = new BufferedReader(fr);

			String sCurrentLine;

			br = new BufferedReader(new FileReader(node));

			while ((sCurrentLine = br.readLine()) != null) {
				
				if(sCurrentLine.startsWith("Lines") && !startFlag){
					startFlag = true;
				}
				else if(startFlag && !"".equals(sCurrentLine.trim())){
					
					//Cleaning and Splitting logic 
					
					String[] words = sCurrentLine.trim().toLowerCase()
							.replaceAll("[1234567890_!#*\\-+.^:,\"\\?><\\\\\\[\\]\\(\\)\\{\\}|=\";]","").split("\\s+");
					
					for(String word : words){
						
						if(word.length()>5)
						{
							if(word.contains("/")){
								String[] wordSplit = word.split("/");
								for(String sWord : wordSplit){
									
									if(sWord.length()>5)
									{
									Integer value = wordMap.get(sWord);
									if(value!=null){
										wordMap.put(sWord, ++value);
									}
									else{
										wordMap.put(sWord, 1);
									}
									}
								}
							}
							else{
								
								Integer value = wordMap.get(word);
								if(value!=null){
									wordMap.put(word, ++value);
								}
								else{
									wordMap.put(word, 1);
								}
							}
						}
					}
				}
				
				//System.out.println(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
	}
	
	
	
}
