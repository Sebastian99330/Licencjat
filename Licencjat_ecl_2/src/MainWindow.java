import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Formatter;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import weka.core.pmml.jaxbbindings.ExponentialSmoothing;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class MainWindow {
	
	/* 	dwuwymiarowa tablica. Zawiera ona wszystkie elementy, tzn to jest dwuwymiarowa tablica. Czyli tablica zawierajaca tablize.
	    * 	Obieg w for'ach po zewnetrznej tablicy to obieg po algorytmach (5 elementow bo 5 algorytmow jest),
	    * 	a obieg w for'ach po wewnetrznej tablicy to obig po wynikach dla poszczegolnych punktow (4 elementy bo 4 punkty)
	    * */
	Vector<Vector<ForecastAlgorithm>> allAlgosAllPoints= new Vector<Vector<ForecastAlgorithm>>();
	
    //countedPredictionOneAlgoAllPoints - policzona predykcja dla jednego algorytmu, dla wszystkich punktów.
    //To tablica zawierajaca 4 elementy. Jeden elemenet to predykcja dla jednego punktu.
	Vector<ForecastAlgorithm> countedPredictionOneAlgoAllPoints= new Vector<ForecastAlgorithm>(4); 
	    
	
	/// Tworzymy wektor pieciu algorytmow. To po to zeby miec kontener na wszystkie algo, zeby je wszystkie odpalic i policzyc ktory jest najlepszy
    Vector<ForecastAlgorithm> onlyAlgorithmsVector = new Vector<ForecastAlgorithm>();
	
	
	// rememberForecastAlgorithm - algorytm, ktorego wyniki ogladac sobie zazyczyl uzytkownik
	private ForecastAlgorithm [] rememberForecastAlgorithm = new ForecastAlgorithm[4];
	private ForecastAlgorithm [] changeForecAlgorithm = new ForecastAlgorithm[4];
	

    //wartosc rmse najskuteczniejszego algorytmu
    double minRMSE=-1.0;
    // theBestAlgorithmNumber - indeks najlepszego algorytmu z tablicy allAlgorithms.
    // ta zmienna bedzie wykorzystana do wyswietlenia na koncu uzytkownikowi ktory algorytm byl najlepszy
	int theBestAlgorithmNumber=-1;
	
	private JFrame frame;
	private JTextField textFieldForecastingLength;
	private JTextField textFieldData;
	
	//givenDate i givenTestSize - ustawienie domyslnych wartosci gdyby uzytkownik nic nie wpisal
	String givenDate="2017-07-22 21:00:00";
	int givenTestSize=48;
	private String changeDate="2017-07-22 21:00:00";
	
	int whichAlgorithmDisplay=0;


	
    // Drop down list. Jest tutaj bo bêdzie update'owany
    JComboBox<Integer> comboBox;
    
    //radio buttony
    JRadioButton rdbtnAvgDay;
    JRadioButton rdbtnAvgWeek;
    JRadioButton rdbtnLinearModel;
    JRadioButton rdbtnGaussianModel;
    JRadioButton rdbtnSVM;
    ButtonGroup radioButtonGroup;
    
    // labele wszystkie
	// Tekst wyswietlajacy wyniki (w lewym dolnym rogu okna programu)
    JLabel [] lblPoint = new JLabel[4];
    
    JLabel lblChooseForecastAlgorithm;
    JLabel lblGetForecastPeriodLength;
    JLabel lblChooseDateStart;
    JLabel lblShowOutcomesFor;
    
    JLabel lblTimeFrames;
    JLabel lblDisplayOutcomes;
    JLabel lblpoWpisaniuWartosci;
    JLabel lblPoWpisaniuWartosci;
    
    JLabel lblNajskuteczniejszyAlgorytm;
    JLabel lblSredniDlaWszystkich;
    
    // whichHour - wyniki dla ktorej godziny przewidywania zyczy sobie widziec uzytkownik?
    // (to sie update'uje w drop down liscie)
	int whichHour=0;
	private JLabel lblopcjaDostepnaPo;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
 
	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}
	
	


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Traffic flow forecasting");
		frame.setBounds(100, 100, 887, 620);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		
		//metoda inicjalizuj¹ca wszystkie radio buttony (dla wyboru algorytmu)
		createRadioButtons();
        

        
		/////Stworzenie guzika na "uruchom obliczenia", oraz dodanie do niego handler'a
		JButton btnRunAlgorithm = new JButton("Uruchom obliczenia");
        HandlerStartButton handlerButton = new HandlerStartButton();
        btnRunAlgorithm.addActionListener(handlerButton);
		btnRunAlgorithm.setBounds(434, 390, 185, 51);
		frame.getContentPane().add(btnRunAlgorithm);
        
		
		//tworzenie JLabeli
		for(int i=0;i<4;i++) {
			lblPoint[i] = new JLabel("Punkt "+(i+1)+": ");
			frame.getContentPane().add(lblPoint[i]);
		}
		createLabels();
		
		///metoda tworzaca wszystkie pola do wczytywania (daty, testSize)
		createTextFields();
		
		//utworzenie drop down listy w metodzie createDropDownList
		createDropDownList(givenTestSize);
		
		/// wstawienie mapy - grafiki	
		JPanel mapPanel = new JPanel();
		mapPanel.setToolTipText("Mapa");
		mapPanel.setBounds(0, 0, 299, 299);
		frame.getContentPane().add(mapPanel);
		
		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("graphic.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		mapPanel.add(picLabel);
		
		lblopcjaDostepnaPo = new JLabel("(Opcja dostepna po obliczeniu najskuteczniejszego algorytmu)");
		lblopcjaDostepnaPo.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblopcjaDostepnaPo.setBounds(10, 467, 381, 14);
		frame.getContentPane().add(lblopcjaDostepnaPo);
		
        //ustawienie domyœlnego algorytmu, na wypadek gdyby u¿ytkownik nie klikn¹³ ¿adnego JRadioButtona
		//pêtla "Enhanced for"
		for(int i=0;i<changeForecAlgorithm.length;i++){
			changeForecAlgorithm[i] = new AverageDay();
		}
        

		
	}
	
    

    //////////////////////////////// METODY INICJALIZUJACE (GUZIKI I INNE KOMPONENTY GUI) ///////////////////////////////////////////////////


	///////////TWORZENIE DROP DOWN LIST
	void createDropDownList(int currentTestSize) {
		Integer[] hoursList = new Integer [currentTestSize];
		for(int i=0;i<currentTestSize;i++) {
			hoursList[i]=i+1;
		}
		//comboBox = new JComboBox<Integer>(hoursList);
		comboBox = new JComboBox<Integer>();
		comboBox.setModel(new DefaultComboBoxModel<Integer>(hoursList));
		
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				whichHour=comboBox.getSelectedIndex();
				//whichHour++;
				updateDisplayedOutcomes();
			}
		});
		
		comboBox.setToolTipText("Ilo\u015B\u0107 godzin do przodu");
		comboBox.setBounds(10, 489, 143, 22);
		frame.getContentPane().add(comboBox);
		
		JLabel lblWyniki = new JLabel("Wyniki:");
		lblWyniki.setBounds(22, 315, 76, 23);
		frame.getContentPane().add(lblWyniki);
		
	}

	void createRadioButtons() {
		rdbtnAvgDay = new JRadioButton("\u015Arednia dzienna", true);
		rdbtnAvgDay.setBounds(434, 147, 273, 23);
		frame.getContentPane().add(rdbtnAvgDay);
		
		rdbtnAvgWeek = new JRadioButton("\u015Arednia tygodniowa");
		rdbtnAvgWeek.setBounds(434, 173, 273, 23);
		frame.getContentPane().add(rdbtnAvgWeek);
		
		rdbtnLinearModel = new JRadioButton("Szeregi czasowe + LM");
		rdbtnLinearModel.setBounds(434, 199, 273, 23);
		frame.getContentPane().add(rdbtnLinearModel);

		rdbtnGaussianModel = new JRadioButton("Proces Gaussa");
		rdbtnGaussianModel.setBounds(434, 225, 273, 23);
		frame.getContentPane().add(rdbtnGaussianModel);
		
		rdbtnSVM = new JRadioButton("Szeregi czasowe + SVM");
		rdbtnSVM.setBounds(434, 251, 273, 23);
		frame.getContentPane().add(rdbtnSVM);
		
		radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(rdbtnAvgDay);
		radioButtonGroup.add(rdbtnAvgWeek);
		radioButtonGroup.add(rdbtnLinearModel);
		radioButtonGroup.add(rdbtnGaussianModel);
		radioButtonGroup.add(rdbtnSVM);
		
		
        //HandlerRadio handlerRadio = new HandlerRadio(0);
        //dodajemy do radiowych guzikow opcje listenerow, czyli: jak klikniemy na guzik, to zeby cos sie dzialo.
        //averageWeek to guzik, i do niego dodajemy listenera
        //HandlerRadio to klasa utworzona do "listener'owania", suzy ona jako listener dla guzika averageWeek (xD).
        rdbtnAvgDay.addActionListener(new HandlerRadio());
        rdbtnAvgWeek.addActionListener(new HandlerRadio());
        rdbtnLinearModel.addActionListener(new HandlerRadio());
        rdbtnGaussianModel.addActionListener(new HandlerRadio());
        rdbtnSVM.addActionListener(new HandlerRadio());
        
	}

	
	void createLabels() {
		
		//komumunikat nad radio buttonami
		lblChooseForecastAlgorithm = new JLabel("Wybierz metod\u0119 przewidywania ruchu:");
		lblChooseForecastAlgorithm.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblChooseForecastAlgorithm.setBounds(434, 126, 273, 14);
		frame.getContentPane().add(lblChooseForecastAlgorithm);
		
		lblGetForecastPeriodLength = new JLabel("Podaj d\u0142ugo\u015B\u0107 przewidywania (w godzinach):");
		lblGetForecastPeriodLength.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblGetForecastPeriodLength.setBounds(434, 305, 273, 14);
		frame.getContentPane().add(lblGetForecastPeriodLength);
		
		
		lblChooseDateStart = new JLabel("Wpisz dat\u0119 (YYYY-MM-DD HH:MM:SS):");
		lblChooseDateStart.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblChooseDateStart.setBounds(434, 21, 273, 14);
		frame.getContentPane().add(lblChooseDateStart);
		
		lblShowOutcomesFor = new JLabel("Wybierz dla ktorej godziny w prz\u00F3d chcesz zobaczyc wyniki:");
		lblShowOutcomesFor.setBounds(10, 453, 381, 14);
		frame.getContentPane().add(lblShowOutcomesFor);
		
		//Wyniki:
		//ustawienie umiejscowienia label'i, nie mozna tego zrobic w for bo znikna te wartosci umiejscowienia
		lblPoint[0].setBounds(10, 374, 407, 14);
		lblPoint[1].setBounds(10, 391, 409, 14);
		lblPoint[2].setBounds(10, 409, 409, 14);
		lblPoint[3].setBounds(10, 428, 409, 14);
		
		
		
		lblTimeFrames = new JLabel("(od 2017-02-03 do 2017-12-17 23:00:00)");
		lblTimeFrames.setBounds(434, 35, 273, 14);
		frame.getContentPane().add(lblTimeFrames);
		
		lblDisplayOutcomes = new JLabel("        Przewidywanie:       Prawdziwe wartosci:          RMSE:");
		lblDisplayOutcomes.setBounds(10, 349, 319, 14);
		frame.getContentPane().add(lblDisplayOutcomes);
		
		lblpoWpisaniuWartosci = new JLabel("Po wpisaniu wartosci nacisnij enter");
		lblpoWpisaniuWartosci.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblpoWpisaniuWartosci.setBounds(434, 52, 273, 14);
		frame.getContentPane().add(lblpoWpisaniuWartosci);
		
		lblPoWpisaniuWartosci = new JLabel("Po wpisaniu wartosci nacisnij enter");
		lblPoWpisaniuWartosci.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblPoWpisaniuWartosci.setBounds(434, 323, 273, 14);
		frame.getContentPane().add(lblPoWpisaniuWartosci);
		
		lblNajskuteczniejszyAlgorytm = new JLabel("Najskuteczniejszy algorytm:");
		lblNajskuteczniejszyAlgorytm.setBounds(10, 512, 364, 14);
		frame.getContentPane().add(lblNajskuteczniejszyAlgorytm);
		
		lblSredniDlaWszystkich = new JLabel("\u015Arednie RMSE najlepszego algorytmu: ");
		lblSredniDlaWszystkich.setBounds(10, 537, 364, 14);
		frame.getContentPane().add(lblSredniDlaWszystkich);
	}
	
	void createTextFields() {
		//guzik wszytujacy testSize
		textFieldForecastingLength = new JTextField();
		textFieldForecastingLength.setText("48");
		textFieldForecastingLength.setBounds(434, 343, 76, 20);
		frame.getContentPane().add(textFieldForecastingLength);
		textFieldForecastingLength.setColumns(10);
		
		//guzik wczytujacy date
		textFieldData = new JTextField();
		textFieldData.setText("2017-07-22 21:00:00");	//2017-07-22 21:00:00
		textFieldData.setBounds(434, 72, 273, 20);
		frame.getContentPane().add(textFieldData);
		textFieldData.setColumns(10);
		
		//dodanie handler'a dla tekstowych pól
		HandlerTextFields handlerTextField = new HandlerTextFields();
		textFieldData.addActionListener(handlerTextField);
		textFieldForecastingLength.addActionListener(handlerTextField);
	}
	
    //////////////////////////////// KONIEC METOD INICJALIZUJACYCH (GUZIKI I INNE KOMPONENTY GUI) ///////////////////////////////////////////////////

	
	
    
    //////////////////////////////// HANDLERY ///////////////////////////////////////////////////
    
    
    /// HANDLER NA POLE TEKSTOWE
    private class HandlerTextFields implements ActionListener {
		//w ActionListener jest tylko jedna metoda.
		//to jest metoda, która jest automatycznie wywo³ywana zawsze, gdy u¿ytkownik kliknie w guzik który ma tego ActionListenera.
		public void actionPerformed(ActionEvent event) {
			//event zawsze wystêpuje, gdy user kliknie w ten guzik.
			if(event.getSource()==textFieldForecastingLength) {
				givenTestSize=Integer.parseInt(event.getActionCommand());
				//NIE WIEM CZEMU ++ ALE DZIEKI TEMU DZIALA DLA OSTATNIEJ LICZBY z dropdown list (inaczej nie dziala sprawdzenie wynikow dla np. 48 godziny w przod)
				//changeTestSize++;
				//updateDropDownList(givenTestSize);	//to jednak nie powinno miec miejsca.				
			}
			else if(event.getSource()==textFieldData) {
				changeDate=String.format(event.getActionCommand());
				MainWindow.this.givenDate=String.format(event.getActionCommand());
			}

		}
	}
    
    
    /////////// HANDLER NA GUZIK ROZPOCZYNAJACY LICZENIE "START"
    private class HandlerStartButton implements ActionListener {    	
        public void actionPerformed(ActionEvent event){   
    		allAlgosAllPoints.removeAll(allAlgosAllPoints);
    		allAlgosAllPoints = new Vector<Vector<ForecastAlgorithm>>();
        	//////////AKTUALIZACJA PARAMETROW DO POLICZENIA
//        	for(int i=0;i<4;i++) {
//        		rememberForecastAlgorithm[i] = changeForecAlgorithm[i];
//	    	}
        	MainWindow.this.givenTestSize=givenTestSize;
        	MainWindow.this.givenDate=changeDate;
        	updateDropDownList(givenTestSize);
        	///// uruchomienie calego liczenia
            makePredictions();
            
        }
    }
    
	
	////////handler dla radio button'ów
    /*
	private class HandlerRadio implements ItemListener {
	    public void itemStateChanged(ItemEvent event){
	    	if(event.getSource()==rdbtnAvgDay) {
	    		whichAlgorithmDisplay=0;
	    	}
	    	if(event.getSource()==rdbtnAvgWeek) {
	    		whichAlgorithmDisplay=1;
	    	}
	    	if(event.getSource()==rdbtnLinearModel) {
	    		whichAlgorithmDisplay=2;
	    	}
	    	if(event.getSource()==rdbtnGaussianModel) {
	    		whichAlgorithmDisplay=3;
	    	}
	    	if(event.getSource()==rdbtnSVM) {
	    		whichAlgorithmDisplay=4;
	    	}
	    	updateDisplayedOutcomes();
	    }
	}
	*/
	private class HandlerRadio implements ActionListener {
	    public void actionPerformed(ActionEvent event){
	    	if(event.getSource()==rdbtnAvgDay) {
	    		whichAlgorithmDisplay=0;
	    	}
	    	else if(event.getSource()==rdbtnAvgWeek) {
	    		whichAlgorithmDisplay=1;
	    	}
	    	else if(event.getSource()==rdbtnLinearModel) {
	    		whichAlgorithmDisplay=2;
	    	}
	    	else if(event.getSource()==rdbtnGaussianModel) {
	    		whichAlgorithmDisplay=3;
	    	}
	    	else if(event.getSource()==rdbtnSVM) {
	    		whichAlgorithmDisplay=4;
	    	}
	    	//System.out.println("Dziwne... "+allAlgosAllPoints.get(2).get(1));
	    	updateDisplayedOutcomes();
	    }
	}
    
    
    //////////////////////////////// KONIEC HANDLEROW ///////////////////////////////////////////////////
	
	
	/////////////////////////////// UPDATE WYSWIETLANIA ITP ///////////////////////////////////////////////////////

	void updateDropDownList(int currentTestSize) {
		comboBox.removeAllItems();
		Integer[] hoursList = new Integer [currentTestSize];
		for(int i=0;i<currentTestSize;i++) {
			hoursList[i]=i+1;
			comboBox.addItem(hoursList[i]);
		}
	}
	
	void updateBestAlgoritmDisplay() {
		//jesli jeszcze nie zostal policzony najlepszy wynik
		if(minRMSE==-1.0 || theBestAlgorithmNumber==-1) {
			return;
		}
		if(theBestAlgorithmNumber==0) {
			lblNajskuteczniejszyAlgorytm.setText("Najskuteczniejszy algorytm: Usrednianie dnia");
		} 
		else if(theBestAlgorithmNumber==1) {
			lblNajskuteczniejszyAlgorytm.setText("Najskuteczniejszy algorytm: Usrednianie tygodnia");
		} 
		else if(theBestAlgorithmNumber==2) {
			lblNajskuteczniejszyAlgorytm.setText("Najskuteczniejszy algorytm: Model Liniowy");
		} 
		else if(theBestAlgorithmNumber==3) {
			lblNajskuteczniejszyAlgorytm.setText("Najskuteczniejszy algorytm: Model Gaussa");
		} 
		else if(theBestAlgorithmNumber==4) {
			lblNajskuteczniejszyAlgorytm.setText("Najskuteczniejszy algorytm: SVM");
		}
		
		/*
		 *  allAlgorithms.addElement(new AverageDay());
			allAlgorithms.addElement(new AverageWeek());
    		allAlgorithms.addElement(new SVM());
    		allAlgorithms.addElement(new GaussianModel());
    		allAlgorithms.addElement(new LinearModel());
		 */
		lblSredniDlaWszystkich.setText("\u015Arednie RMSE najlepszego algorytmu: "+minRMSE);
	}	
	
	
    
    void displayError(int errorNumber, int whichPoint) {
    	//JOptionPane.showMessageDialog(frame, "Eggs are not supposed to be green.");
    	switch (errorNumber) {
		case 1:
			JOptionPane.showMessageDialog(frame, "Nie udalo sie wczytac pliku z danymi dla punktu "+whichPoint+".");
			System.exit(0);
			break;
		case 2:
			//JOptionPane.showMessageDialog(frame, "Wprowadzono niepoprawna date lub wystepuje brak danych dla danej daty. Prosze wprowadzic date ponownie.");
			break;
		case 3:
			JOptionPane.showMessageDialog(frame, "Nie mozemy porownac tylu godzin w przód od podanej daty \ndla punktu "+whichPoint+".");
			//System.exit(0);
			break;
		case 4:
			JOptionPane.showMessageDialog(frame, "Wystapil blad podczas budowania modelu regresji \ndla punktu "+whichPoint+". \nKoniec programu.");
			System.exit(0);
			break;
		case 5:
			//brak danych
			//JOptionPane.showMessageDialog(frame, "W zbiorze posrod danych wystapil brak. Przerwano przewidywanie");
			//System.exit(0);
			break;
		case 6:
			//JOptionPane.showMessageDialog(frame, "Nie mozemy tyle sie cofnac w czasie od podanej daty \ndla punktu "+whichPoint+".");
			//System.exit(0);
			break;
		case 7:
			//JOptionPane.showMessageDialog(frame, "Wpisano zbyt pozna date.");
			//System.exit(0);
			break;
		default:
			JOptionPane.showMessageDialog(frame, "Wystapil nieznany blad podczas proby przewidywania danych \ndla punktu "+whichPoint+".");
			System.exit(0);
			break;
		}
    }
    
    //updateDisplayedOutcomes - update wyswietlanych wartosci dla kazdego punktu. Wyniku predykcji, prawdziwej wartosci i rmse. 
    void updateDisplayedOutcomes() {
    	//jesli predykowanie przebieglo pomyslnie, i mamy wyniki, to je wypisujemy
    	if(allAlgosAllPoints.size()==0) {
    		//displayError(allAlgosAllPoints[0][0].errorNumber, whichPoint);
    		return;
    	}
    	

    	//obieg ii to obieg po wartosciach dla punktow od 1 do 4
    	//allAlgosAllPoints.get(i).get(ii)
    	for(int i=0;i<4;i++) {
    		//jesli jeszcze nie rozpoczelismy liczenia, to nic nie zmieniamy w wyswietlaniu wynikow (chyba zbedny warunek ale niech zostanie)
        	if(allAlgosAllPoints.get(whichAlgorithmDisplay).size()==0){
        		return;
        	}
        	
        	
        	//jesli nie ma braku wartosci dla wartosci prawdziwej albo oszacowanej
	        if(allAlgosAllPoints.get(whichAlgorithmDisplay).get(i).predictedValues!=null && allAlgosAllPoints.get(whichAlgorithmDisplay).get(i).trueValues!=null) {
	        	/////// wyswietlanie wynikow dla wszystkich punktow po kolei
	        	//zakraglamy wartosci dla wyswietlania
	        	double predValue = Math.round((allAlgosAllPoints.get(whichAlgorithmDisplay).get(i).predictedValues[whichHour])*100);		        	
	        	predValue/=100;
	        	double trueValue = Math.round((allAlgosAllPoints.get(whichAlgorithmDisplay).get(i).trueValues[whichHour])*100);
	        	trueValue/=100;
	        	double rmseValue =  Math.round((allAlgosAllPoints.get(whichAlgorithmDisplay).get(i).RMSE)*100);
	        	rmseValue/=100;
	        	
	        	//jak juz mamy wartosci potrzebne do wyswietlenia wynikow, to wyswietlamy je
	        	lblPoint[i].setText("Punkt "+(i+1)+":   "+predValue+"                              "+
															trueValue+"                 "+rmseValue);
	        	//linijka zeby na pewno wypisalo dane na okno (inaczej tego nie robi)
	        	lblPoint[i].paintImmediately(lblPoint[i].getVisibleRect());
	        	
        		//linijki zeby na pewno wypisalo dane na okno (inaczej tego nie robi)
        		lblNajskuteczniejszyAlgorytm.paintImmediately(lblNajskuteczniejszyAlgorytm.getVisibleRect());
        		lblSredniDlaWszystkich.paintImmediately(lblSredniDlaWszystkich.getVisibleRect());
	        }
	        else {	//czyli jesli jest byl brak wartosci -1 dla zbioru treningowego albo testowego, to wyswietlamy ze brak i nic
	        	lblPoint[i].setText("Punkt "+(i+1)+": Niepoprawna data lub wystepuje brak danych dla danej daty.");
	    		lblNajskuteczniejszyAlgorytm.setText("Najskuteczniejszy algorytm: ");
	    		lblSredniDlaWszystkich.setText("\u015Arednie RMSE najlepszego algorytmu: ");
	    		lblPoint[i].paintImmediately(lblPoint[i].getVisibleRect());
	        }

    	}
	    updateBestAlgoritmDisplay();
	    
    }


    
	
	/////////////////////////////// KONIEC UPDATE WYSWIETLANIA ITP ///////////////////////////////////////////////////////

    ///////////////////////////////////// INNE (METODY ROZNE) ////////////////////////////////////////////

	// createNewAlgorithmObj - metoda tworzaca nowy obiekt dla wybranego algorytmu. 
	// To na wypadek, gdyby uzytkownik najpierw wpisal dobra date i uruchomil algorytm, potem tylko wpisal nowa - ZLA date, i uruchomil program.
	// Trzeba na taki wypadek stworzyc nowe przewidywanie dla nowej daty.
	// metoda jest wywolywana po wprowadzeniu kazdego parametru. Daty, testSize, oraz wyboru algorytmu.
	ForecastAlgorithm createNewAlgorithmObj(ForecastAlgorithm newForeAlg, String subClassName) {
		if(subClassName.equalsIgnoreCase("AverageDay")) {
    		newForeAlg = new AverageDay();
    	}
    	if(subClassName.equalsIgnoreCase("AverageWeek")) {
    		newForeAlg = new AverageWeek();
    	}
    	if(subClassName.equalsIgnoreCase("LinearModel")) {
    		newForeAlg = new LinearModel();
    	}
    	if(subClassName.equalsIgnoreCase("GaussianModel")) {
    		newForeAlg = new GaussianModel();
    	}
    	if(subClassName.equalsIgnoreCase("SVM")) {
    		newForeAlg = new SVM();
    	}

		return newForeAlg;		
	}
	

	// countBestAlgorithm - metoda znajdujaca najlepszy algorytm (glowna rzecz w programie).
	// To metoda, ktora odpala wszystkie algorytmy dla wszystkich punktow
	// po kolei dla kazdego algorymu, liczy srednia RMSE z kazdego z czterech punktow
	// czyli po tym kroku ma kazdy algorytm i RMSE z niego
	// na koncu wybiera jaki algorytm ma najmniejsze RMSE
	// i zwraca ten algorytm (w sensie zapisuje jego indeks w tablicy algorytmow)
	void makePredictions() {

		
		
	    
	    

	    
	    //countedPredictionOneAlgoAllPoints - policzona predykcja dla jednego algorytmu, dla wszystkich punktów.
	    //To tablica zawierajaca 4 elementy. Jeden elemenet to predykcja dla jednego punktu.
	    
    	onlyAlgorithmsVector.addElement(new AverageDay());
    	onlyAlgorithmsVector.addElement(new AverageWeek());
    	onlyAlgorithmsVector.addElement(new LinearModel());
    	onlyAlgorithmsVector.addElement(new GaussianModel());
    	onlyAlgorithmsVector.addElement(new SVM());
    	
    	
    	
    	//obchodzimy wszystkie algorytmy, zeby wywolac metode 
    	for(int i=0;i<onlyAlgorithmsVector.size();i++) {
    			Vector<ForecastAlgorithm> countedPredictionOneAlgoAllPoints= new Vector<ForecastAlgorithm>(4); 

				//onlyAlgorithmsVector.get(i) to obiekt FOrecastAlgorithm, na przyk³ad AverageDay.
    			countedPredictionOneAlgoAllPoints.addElement(onlyAlgorithmsVector.get(i).run(MainWindow.this.givenDate, MainWindow.this.givenTestSize, "punkt-1.csv"));
    			//allAlgosAllPoints.addElement(countedPredictionOneAlgoAllPoints);
    			
    			//tworzymy nowy objekt ForecastAlgo. Trzeba zeby wartosci nie znikaly
    			try {
    				onlyAlgorithmsVector.set(i,onlyAlgorithmsVector.get(i).getClass().newInstance());
    			}catch(Exception e) {e.printStackTrace();}
    			
    			countedPredictionOneAlgoAllPoints.addElement(onlyAlgorithmsVector.get(i).run(MainWindow.this.givenDate, MainWindow.this.givenTestSize, "punkt-2.csv"));
    			//allAlgosAllPoints.addElement(countedPredictionOneAlgoAllPoints);
    			try {
    				onlyAlgorithmsVector.set(i,onlyAlgorithmsVector.get(i).getClass().newInstance());
    			}catch(Exception e) {e.printStackTrace();}
    			
    			countedPredictionOneAlgoAllPoints.addElement(onlyAlgorithmsVector.get(i).run(MainWindow.this.givenDate, MainWindow.this.givenTestSize, "punkt-3.csv"));
    			//allAlgosAllPoints.addElement(countedPredictionOneAlgoAllPoints);
    			try {
    				onlyAlgorithmsVector.set(i,onlyAlgorithmsVector.get(i).getClass().newInstance());
    			}catch(Exception e) {e.printStackTrace();}
    			
    			countedPredictionOneAlgoAllPoints.addElement(onlyAlgorithmsVector.get(i).run(MainWindow.this.givenDate, MainWindow.this.givenTestSize, "punkt-4.csv"));
    			allAlgosAllPoints.addElement(countedPredictionOneAlgoAllPoints);
		        
    			
		        
		        //dodajemy wektor z obliczeniami z wszystkich punktow do wektora ktory ma taki obiekt ale dla kazdego algorytmu
		        //lecimy po ca³ym wektorze all algos all points i pojedynczo dodajemy
		        //for (int ii = 0; ii < countedPredictionOneAlgoAllPoints.length; ii++) {
			    //    [ii]=countedPredictionOneAlgoAllPoints[i];

				//}
		        
	            //jesli wystapily bledy, to wyswietlamy je
		        //ii<4 bo oblatujemy dla wszystkich punktow (ktorych jest 4)
	            for(int ii=0;ii<4;ii++) {
            		if(allAlgosAllPoints.get(i).get(ii).errorNumber!=0) {
	            		displayError(allAlgosAllPoints.get(i).get(ii).errorNumber, ii);
	            	}
	            }
    	}
    	
    	
    	// Wektor na srednie (srednie po punktach) RMSE wszystkich algorytmow. Potem wybierzemy ktore RMSE jest najmniejsze czyli ktory algorytm policzyl najlepiej
    	Vector<Double> allRmseVector = new Vector<Double>();
    	////iterujemy po calym all algos all points, zeby znalezc wszystkie rmse (dla wszystkich algo i dla wszystkich pktow)
    	for (int i = 0; i < allAlgosAllPoints.size(); i++) {
    		double meanRmseByPoins=0.0;
    		for (int j = 0; j < allAlgosAllPoints.get(i).size(); j++) {
    			//sumujemy rmse dla jednego algorytmu dla wszystkich punktow
				meanRmseByPoins+=allAlgosAllPoints.get(i).get(j).RMSE;
			}
    		//dzielimy przez liczbe punktow
    		meanRmseByPoins/=allAlgosAllPoints.get(i).size();
			//wynig wsadzamy do wektora na srednie wszystkich algorytmow (zeby potem z niego wybrac minimum)
    		allRmseVector.addElement(meanRmseByPoins);
		}
    	
    	//czyli teraz mamy allRmseVector, ktory ma tyle elementow co jest algorytmow (czyli 5)
    	//i ma dla kazdego z nich srednia wartosc z RMSE swoich obliczen dla czterech punktow.
    	//teraz trzeba wybrac minimum z niego i wyswietlic dla ktorego algorytmu to bylo.
    	
	    

		
		
		minRMSE=Collections.min(allRmseVector);
		theBestAlgorithmNumber=allRmseVector.indexOf(minRMSE);

		//zaokraglenie do 2 wartosci po przecinku
		minRMSE=Math.round((minRMSE)*100);
		minRMSE/=100;
			
		//mamy wszystko i teraz musimy to wyswietlic
		// updateBestAlgoritmDisplay(); // to usunac
		updateDisplayedOutcomes();
	} // zamyka cala metode countBestAlgorithm
	
	
	
	///////////////////////////////////// KONIEC INNYCH (ROZNYCH METOD) ////////////////////////////////////////////

	/////////////////////////////////////// WYPISANIE DO PLIKU ////////////////////////////////////////////
	void writeOutputsToFile() {
		Formatter x1;
		Formatter x2;
		Formatter x3;
		Formatter x4;
		Formatter x5;
		Formatter x6;
		try {
			x1= new Formatter("true.dat");
			x2= new Formatter("dzien.dat");
			x3= new Formatter("tydzien.dat");
			x4= new Formatter("liniowy.dat");
			x5= new Formatter("gauss.dat");
			x6= new Formatter("svm.dat");
		

			
	        for(int j=0;j<48;j++) {
	        	Integer trueVal;
	        	Integer predVal;
	        	try {
					trueVal=allAlgosAllPoints.get(0).get(0).trueValues[j].intValue();
					x1.format("%s %s\n", j, trueVal.toString());
		        	predVal=allAlgosAllPoints.get(0).get(0).predictedValues[j].intValue();
					x2.format("%s %s\n", j, predVal.toString());
		        	predVal=allAlgosAllPoints.get(1).get(0).predictedValues[j].intValue();
					x3.format("%s %s\n", j, predVal.toString());
					predVal=allAlgosAllPoints.get(2).get(0).predictedValues[j].intValue();
					x4.format("%s %s\n", j, predVal.toString());
					predVal=allAlgosAllPoints.get(3).get(0).predictedValues[j].intValue();
					x5.format("%s %s\n", j, predVal.toString());
					predVal=allAlgosAllPoints.get(4).get(0).predictedValues[j].intValue();
					x6.format("%s %s\n", j, predVal.toString());
	        	}catch (Exception e) {
					// TODO: handle exception
	        		e.printStackTrace();
				}
	        }
			

			
			
			x1.close();
			x2.close();
			x3.close();
			x4.close();
			x5.close();
			x6.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	////////////////////////////////////////// KONIEC WYPISANIA DO PLIKU ////////////////////////////////////

}



