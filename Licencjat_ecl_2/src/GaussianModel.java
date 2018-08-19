import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;

import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.timeseries.*;
import weka.core.Instances;

public class GaussianModel extends ForecastAlgorithm{
    public ForecastAlgorithm run(String givenDate, int testSize, String fileName) {
    	super.run(givenDate, testSize, fileName);
        if(this.errorNumber!=0) {
        	return this;
        }
        
        System.out.println("\n\nPrzewidywanie modelem PROCESU GAUSSA na podstawie zebranych "+trainSize+" danych:");
        
        this.windowSize = 4;
        
        // new forecaster
        weka.classifiers.timeseries.WekaForecaster forecaster = new weka.classifiers.timeseries.WekaForecaster();
        java.util.List<java.util.List<NumericPrediction>> forecast = null;
        try{
        	//wybieramy pole do predyckji
	        forecaster.setFieldsToForecast("flow");
	        //tworzymy klasyfikator
	        GaussianProcesses gauss_reg=new GaussianProcesses();
	        //ustwiamy parametry klasyfikatora
	        forecaster.setBaseForecaster(gauss_reg);
	        forecaster.getTSLagMaker().setMinLag(1);
	        forecaster.getTSLagMaker().setMaxLag(this.testSize*this.windowSize);
	        forecaster.getTSLagMaker().setTimeStampField("idx");
	        //budujemy model predykcyjny time series
	        System.out.println("Przed stworzeniem modelu");
	        forecaster.buildForecaster(this.train, System.out);
	        System.out.println("Po stworzeniu modelu");
	        forecaster.primeForecaster(this.train);
	        forecast = forecaster.forecast(this.testSize, System.out);
        }
        catch(Exception e){
        	System.out.println("TO SIE NAWET NIE WYSWIETLA");
        	e.printStackTrace();
            //JTextField textField= new JTextField("Nie udalo sie zbudowac klasyfikatora, konczymy program!",20);
            //frame.add(textField);
            this.errorNumber=4;	//blad przy budowaniu modelu regresji Gaussa
            return this;
        }
        //wartości predukowanych ma byc tyle ile wynosi test_size
        this.predictedValues=new Double[this.testSize];
    	this.absError=new Double[this.testSize];
    	double mean_squared_error =0;
        for(int i=0; i<this.testSize; i++)
        {
        	java.util.List<NumericPrediction> predsAtStep = forecast.get(i);
        	double predicted = predsAtStep.get(0).predicted();
        	this.predictedValues[i]=predicted;
        	this.absError[i]=Math.abs(this.predictedValues[i]-this.trueValues[i]);
        	mean_squared_error=mean_squared_error+this.absError[i]*this.absError[i];
        }
        System.out.println(mean_squared_error);
        mean_squared_error=mean_squared_error/this.testSize;

        System.out.println("Wypisanie przewidywań dla dat");
        //na koniec wypisanie statystyk
        /*System.out.println("Date \t\t\t true \t predicted \t error");
        for(int i=0; i<this.testSize; i++)
        {
        	int idx=this.currentIdx+i;
        	String readDate = this.data.instance(idx).stringValue(this.data.attribute("data"));
        	System.out.println(readDate
        					+"\t"+this.trueValues[i]
        					+"\t"+this.predictedValues[i]
                			+"\t\t"+this.absError[i]);
        }*/
        System.out.println("mean squared error (MSE) = "+ mean_squared_error);
        System.out.println("root-mean-square error (RMSE) = "+ Math.sqrt(mean_squared_error));
        
        this.MSE=mean_squared_error;
        this.RMSE = Math.sqrt(mean_squared_error);
        return this;
    }
}