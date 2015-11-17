import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


public class Perceptrons {
	static String TrainHamPath=null; // holds training data ham path folder name
	static String TrainSpamPath=null; // holds training data spam path folder name
	static String TestHamPath=null;  // holds test data ham path folder name
	static String TestSpamPath=null;// holds test data spam path folder name
	static String StopWordsPath=null; // hold stopwords foldername
	static String[][] word_list =null;
	static String[][] weight_list=null;	
	static int train_tot_docs =0;
	static int train_tot_ham =0;
	static int train_tot_spam =0 ;
	static LinkedHashMap<String,Integer> HamMap = new LinkedHashMap<String, Integer>(); // holds the word and occurence of it in ham folder files
	static LinkedHashMap<String,Integer> SpamMap = new LinkedHashMap<String,Integer>(); // holds the word and occurence of it in spam folder files
	static LinkedHashMap<String,Integer> Dis_Words_Map = new LinkedHashMap<String,Integer>(); // holds the distict words both in ham and spam fold
	static double N=0.743; 
        static int limit= 15;
	
	static LinkedList<String> stop_list1 = new LinkedList<String>();
	 static LinkedList<String> spl_char1 =new LinkedList<String>();
	 

	
	static double w0 =0.1;
	static LinkedHashMap<String, Double> weight_Map=new LinkedHashMap<String, Double>();

	public Perceptrons(String path1,String path2,String path3,String  path4,String path5)
	{
		this.TrainHamPath=path1;
		this.TrainSpamPath=path2;
		this.TestHamPath=path3;
	    this.TestSpamPath=path4;	
	    this.StopWordsPath=path5;
		
	}
	
	// Method to do Perceptrons
	public static void do_Perceptrons() throws FileNotFoundException
	{
		
		train_tot_ham=total_docs_folder(TrainHamPath);
		train_tot_spam=total_docs_folder(TrainSpamPath);
		train_tot_docs = train_tot_ham + train_tot_spam;
		
		store_stopwords(StopWordsPath);
		 create_special_char_list();
		 
			count_words(TrainHamPath, HamMap);// count words in Ham train folder and store in the HamMap
			count_words(TrainSpamPath, SpamMap);// count words in Ham train folder and store in the HamMap
			int ham_tot_words = count_tot_words(HamMap);
			int spam_tot_words = count_tot_words(SpamMap);
			int dis_total_words =count_tot_dis_words(HamMap,SpamMap);
			
			 word_list= new String [train_tot_docs+1][dis_total_words+1];
			 create_word_list(Dis_Words_Map,train_tot_docs,dis_total_words,TrainHamPath,TrainSpamPath);
			 weight_list=new String[2][dis_total_words];
			 
			 create_weight_list(Dis_Words_Map,dis_total_words);
			 
			 cal_weight_list(dis_total_words, train_tot_docs);
		 
		
	} // end of do_perceptrons
	
	
	
	// method to calculate the weight list with new values 
		public static void cal_weight_list( int col , int doc_count ) throws FileNotFoundException
		{
			
			
		
				System.out.println("N= " +N + " and Limit is : "+limit);
				
				for(int l=0;l<limit;l++)
				{
				
				//	 System.out.println("---------Iteration--------"+ (limit+1));
					 
					 int count=0;
					 
					 for(int doc=1;doc<word_list.length;doc++){

						 int prior= cal_prior(doc,word_list);
						 
						 if(prior == 1 && (word_list[doc][col]).equalsIgnoreCase("ham")){
							 count++;
						   }
						   else if(prior == 1 && (word_list[doc][col]).equals("spam")){
							   //recalculate weights
							   update_weight_list(doc,prior,word_list,col);
						   }
						   else if(prior == -1 && (word_list[doc][col]).equals("spam")){
							   count++;
						   }
						   else if(prior == -1 && (word_list[doc][col]).equals("ham")){
							   update_weight_list(doc,prior,word_list,col);
						   }
						 
					 } // end of for doc
					 
					
					
	//				 System.out.println("accuracy in round this round is:"+((count*100)/(doc_count)));
					
				} // end of for loop limit
				
				 for(int i=0;i<Dis_Words_Map.size();i++){
						String word=weight_list[0][i];
						Double wI=Double.parseDouble(weight_list[1][i]);
						weight_Map.put(word, wI);
					}
				 System.out.println("\nChecking accuracy for Perceptrons without removing Stopwords");
				cal_accuracy(TestHamPath, 1,"no");
				cal_accuracy(TestSpamPath, 2,"no");
				
				 System.out.println("\nChecking accuracy for Perceptrons after removing Stopwords");
					cal_accuracy(TestHamPath, 1,"yes");
					cal_accuracy(TestSpamPath, 2,"yes");
				
		
			
			
		} // end of method cal_weight_list
		
		
	// method to calculate the accuracy of the test data
		
		public static void cal_accuracy(String file,int type, String stop ) throws FileNotFoundException
		{
			int t=0;
			File folder = new File(file);
			File[] files = folder.listFiles();
			if (type==1)
			{
				t=1;
			}
			else
			{
				t=-1;
			}
				int count=0;
			// for every file in the folder do
			for(int i=0;i<files.length;i++)
			{
				double HamProb=0,SpamProb=0,sum=0;
				LinkedHashMap<String,Integer> temp_map = new LinkedHashMap<String,Integer>(); 
			
				File cur_file = files[i];
			
			//for every doc create a temp_map with words and its count
				temp_map=count_doc_words(cur_file.toString(),temp_map,stop);
		
				// calculate summation of WI and Xi for O
				double O=0;
		Set set= temp_map.entrySet();
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			Map.Entry me = (Map.Entry)it.next();
			String word= me.getKey().toString();	
			Double xI=Double.parseDouble(me.getValue().toString());
			double wI=0;
			if(weight_Map.containsKey(word))
			{
				wI= weight_Map.get(word);
			
				
			}
			
			O=O+(wI*xI);
}// end of while for every word in the doc
		
		O=w0+O;
		
	
		if(t==1){
			 if(O>0)
				 count++;
		 }
		 else if(t==-1){
			 if(O<0)
				 count++;
		 }
				
		
			}//end of for i 
			
			
			
			if(type== 1)
				 System.out.println("Perceptron Accuracy on test data for Ham Class Is="+ (count*100)/files.length);
			 else
				 System.out.println("Perceptron Regression Accuracy on test data for Spam class Is="+ (count*100)/files.length);
			
			
		}// end of method cal_accuracy
		
		
		
		// method to count the words in a single doc
		public static LinkedHashMap<String,Integer> count_doc_words(String path,LinkedHashMap<String,Integer> map,String stop) throws FileNotFoundException
		{
			
				File file =new File(path);
				Scanner scan = new Scanner(file);
				while(scan.hasNext())
				{
					String word =scan.next();
					
					if(spl_char1.contains(word)){
						 continue;
					 }//end of if
					if(stop.equalsIgnoreCase("yes") && stop_list1.contains(word))
					{
						continue;
					}
					if(map.containsKey(word))
					 {
					 Integer count = (int)map.get(word);
					 count= count+1;
					 map.remove(word);
					 map.put(word, count);
						 
					 }//end of else if
					
					 else
						 map.put(word, 1);
				}// end of while
			return map;
		}// end of method count_words
		
		
	
		// method to update weight list
		public static void update_weight_list(int doc_no, int O, String[][] word_list, int type)
		{
			
			String file_type= word_list[doc_no][type];
			int t=0;
			if(file_type.equalsIgnoreCase("ham"))
			{
				t=1;
			}
			else
			{
				t=-1;
			}
			for(int i=0;i<Dis_Words_Map.size();i++){
				
				double wI= Double.parseDouble(weight_list[1][i]);
				wI=wI+((N)*(t-O)*Integer.parseInt(word_list[doc_no][i]));
				weight_list[1][i]=String.valueOf(wI);
				
			}//end of for i
		} // end of method update_weight_list
		
		// method to calculate document prior 
		
		public static int cal_prior(int doc_no, String[][] word_list)
		{
			
			double O=0;
			int prior_val=0;
			
			for(int i=0;i< Dis_Words_Map.size();i++)
			{
				
				int xI=Integer.parseInt(word_list[doc_no][i]);
				double wI=Double.parseDouble(weight_list[1][i]);
				O=O+(xI*wI);
			}// end of for i
			O=O+w0;
			
			if(O >0)
			{
				prior_val=1;
			}
			else
			{
				prior_val=-1;
			}
			
			return prior_val;
		}// end of method cal_prior
		
		
		
	
	//method to create weight list
	
	public static void create_weight_list(LinkedHashMap<String,Integer> map,int col)
	{
		Set set = map.entrySet();
		Iterator it=set.iterator();
		int i=0;
		while(it.hasNext())
		{
			Map.Entry me = (Map.Entry)it.next();
			weight_list[0][i]= me.getKey().toString();
			i++;
			
		}
		
		for(int j=0;j<col;j++)
		{
			String wI=random_number_generator(0,3);
			weight_list[1][j]=wI;
		}
		

		
	}// end of method create_weight_list
	
	
	// method to generate random number
	public static String random_number_generator(float min, float max)
	{
		Random r= new Random();
		float r_no = (r.nextFloat()*(max-min))+min;
	//	System.out.print("\n the random is: "+r_no);
		
		return String.valueOf(r_no);
	}
	
	// method to create word list
			public static void  create_word_list(LinkedHashMap<String,Integer> map,int row, int colm, String HamPath,String SpamPath) throws FileNotFoundException
			{
				int doc_no=1; // row no of word_list matrix
				Set set = map.entrySet();
				Iterator it = set.iterator();
				int col=0;
				while(it.hasNext())
				{
					Map.Entry me = (Map.Entry)it.next();
					word_list[0][col]=me.getKey().toString();
					col++;
				}
				word_list[0][colm]= "class_name";
				for(int i=1;i<word_list.length;i++)
				{
					for(int j=0;j<word_list[i].length-1;j++)
						{
						word_list[i][j]="0";
						}
				}
				
				
				
				// Intialize the word_list with ham words occurence

				File folder1 = new File(HamPath);
				File[] files = folder1.listFiles();
				for(int x=0;x<files.length;x++)
				{
					LinkedHashMap<String,Integer> temp_map = new LinkedHashMap<String,Integer>(); 
					File cur_file= files[x];
					String word=null;
					Scanner scan = new Scanner(cur_file);
					while(scan.hasNext())
					{
						word=scan.next();
						if(spl_char1.contains(word)){
							 continue;
						 }//end of if
						 else if(temp_map.containsKey(word))
						 {
						 Integer count = (int)temp_map.get(word);
						 count= count+1;
						 temp_map.remove(word);
						 temp_map.put(word, count);
							 
						 }//end of else if
						 else
						 temp_map.put(word, 1);
						
				}// end of while
					String value=null;
					String key=null;
					Set set1 = temp_map.entrySet();
					Iterator it1 = set1.iterator();
					while(it1.hasNext())
					{
						Map.Entry me = (Map.Entry)it1.next();
						key=me.getKey().toString();
						value= me.getValue().toString();
						for	(int i=0;i <colm ;i++)		
						{
							if(word_list[0][i].equalsIgnoreCase(key))
							{
								
								word_list[doc_no][i]=value;
							//	continue;
							}// end of if
						} // end of for
						
						
					}// end of while
					word_list[doc_no][colm]="ham";
					doc_no++;
				}// end of for x
			
				// Intialize the word_list with spam words occurence

				File folder2 = new File(SpamPath);
				File[] files1 = folder2.listFiles();
				for(int y=0;y<files1.length;y++)
				{
					LinkedHashMap<String,Integer> temp_map1 = new LinkedHashMap<String,Integer>(); 
					File cur_file1= files1[y];
					String word1=null;
					Scanner scan1 = new Scanner(cur_file1);
					while(scan1.hasNext())
					{
						word1=scan1.next();
						if(spl_char1.contains(word1)){
							 continue;
						 }//end of if
						 else if(temp_map1.containsKey(word1))
						 {
						 Integer count1 = (int)temp_map1.get(word1);
						 count1= count1+1;
						 temp_map1.remove(word1);
						 temp_map1.put(word1, count1);
							 
						 }//end of else if
						 else
						 temp_map1.put(word1, 1);
						
				}// end of while
					String value1=null;
					String key1=null;
					Set set1 = temp_map1.entrySet();
					Iterator it1 = set1.iterator();
					while(it1.hasNext())
					{
						Map.Entry me1 = (Map.Entry)it1.next();
						key1=me1.getKey().toString();
						for	(int i=0;i <colm ;i++)		
						{
							if(word_list[0][i].equalsIgnoreCase(key1))
							{
								value1= me1.getValue().toString();
								word_list[doc_no][i]=value1;
							//	continue;
							}// end of if
						} // end of for
						
					}// end of while
					word_list[doc_no][colm]="spam";
					doc_no++;
				}// end of for y
			
			
			}//end of method create_word_list
			
	
	// method to calculate total no of distinct words in a both spam and ham  folder 
	
	public static int count_tot_dis_words(LinkedHashMap Hmap, LinkedHashMap Smap)
	{
		int count=0;
		Integer temp=1;
		Set set1 = Hmap.entrySet();
		Iterator it1 = set1.iterator();
		while(it1.hasNext())
		{
			 Map.Entry me1 = (Map.Entry)it1.next();
	       //  System.out.print(me.getKey() + ": ");
	      //   System.out.println(me.getValue());
	       
	         
	     //    System.out.println("The count is : " +count);
	         Dis_Words_Map.put(me1.getKey().toString(), 1);
		}
		
		
		Set set2 = Smap.entrySet();
		Iterator it2 = set2.iterator();
		while(it2.hasNext())
		{
			 Map.Entry me2 = (Map.Entry)it2.next();
	      
			 if(Dis_Words_Map.containsKey(me2.getKey()))
				 continue;
			 else
	         Dis_Words_Map.put(me2.getKey().toString(), 1);
		}
		
		count =Dis_Words_Map.size();
		
	return count;
		
	}
	
	// method to calculate total no of words in a one folder 
	public static int count_tot_words(LinkedHashMap map)
	{
		int count =0;
		Set set = map.entrySet();
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			 Map.Entry me = (Map.Entry)it.next();
	   
	         count = count + (int)me.getValue();
		}
		
		return count;
		
	}
	
	
	
	// method to count the words in the folder 
	
	public static void count_words(String path,LinkedHashMap map) throws FileNotFoundException
	{
		File folder = new File(path);
		File[] files = folder.listFiles();
		
		for(int i=0;i<files.length;i++)
		{
			File file = files[i];
			Scanner scan = new Scanner(file);
			while(scan.hasNext())
			{
				String word =scan.next();
				
				 if(spl_char1.contains(word)){
					 continue;
				 }//end of if
				 else if(map.containsKey(word))
				 {
				 Integer count = (int)map.get(word);
				 count= count+1;
				 map.remove(word);
				 map.put(word, count);
					 
				 }//end of else if
				 else
					 map.put(word, 1);
			}// end of while
		}// end of for
	}// end of method count_words
	
	
	
	
	
	
  //method to create special characters list
    
    public static void create_special_char_list()
    {
        spl_char1.add(".");
        spl_char1.add("-");
        spl_char1.add(":");
        spl_char1.add("/");
        spl_char1.add("@");
        spl_char1.add(",");
        spl_char1.add(" ");
        spl_char1.add("%");
        spl_char1.add("!");
        spl_char1.add(">");
        spl_char1.add("<");
        spl_char1.add("=");
        spl_char1.add("?");
        spl_char1.add("[");
        spl_char1.add("]");
        spl_char1.add("'");
        spl_char1.add("{");
        spl_char1.add("}");
        spl_char1.add("");
        spl_char1.add("_");
        spl_char1.add("$");
        spl_char1.add("(");
        spl_char1.add(")");
        spl_char1.add("|");
        spl_char1.add("*");
        spl_char1.add("~");
        spl_char1.add("`");
        spl_char1.add(".");
        spl_char1.add("#");
        spl_char1.add("\"");
        spl_char1.add("+");
        
    }
	
	//method to calculate total no of documents in a folder
		public static int total_docs_folder(String path)
		{
			return new File(path).listFiles().length;
		}// end of total_docs_folder method
		
		
		
		
		//method to store stopwords into array list
		public static void store_stopwords(String file) throws FileNotFoundException
		{
				String word=null;
				File cur_file= new File(file);
				 Scanner scan=new Scanner(cur_file);
				while(scan.hasNext())
				{
					word=scan.next();
					stop_list1.add(word);
					
					
				}// end of while
				
			
		}// end of method store_stopwords
	
}
