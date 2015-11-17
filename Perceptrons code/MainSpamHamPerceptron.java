import java.io.FileNotFoundException;


public class MainSpamHamPerceptron {

	public static void main(String[] args) throws FileNotFoundException {

		
		Perceptrons per = new Perceptrons(args[0],args[1],args[2],args[3],args[4]);
		
		per.do_Perceptrons();
		

	}

}
