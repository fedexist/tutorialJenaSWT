package tutorialJena;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Uid;

import org.apache.jena.query.* ;

public class HelloWorld {
	
	
	//Sorgenti staiche di dati da importare nel modello
	static private String personURI = "https://fleanend.github.io/";
	private static String fullNameArray[] = { "Federico D'Ambrosio", "Enrico Ferro", "Edoardo Ferrante", "Giulia Cagnes" };
	private static String phoneArray[] = {"0000001", "0000002", "0000003", "0000004" };
	

	public HelloWorld() {
		// TODO Auto-generated constructor stub
	}
	
	//Http GET usata per salvare il VCF
	public static String getVCF(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line + "\n");
		}
		rd.close();
		return result.toString();
	}
	
	public static void main(String[] args){
		
		Calendar c = Calendar.getInstance();
		
		Model model = ModelFactory.createDefaultModel();
		
		ArrayList<String> URIarray = new ArrayList<>();
		ArrayList<Resource> ResourceArray = new ArrayList<>();
		ArrayList<String> givenNameArray = new ArrayList<>();
		ArrayList<String> familyNameArray = new ArrayList<>();
		ArrayList<VCard> vcardArray = new ArrayList<>();
				
		//Riempimento di array per family e given name, creazione URI
		for(String name : fullNameArray){
			URIarray.add(personURI + name.replaceAll(" " , "-").replace("\'", "-"));
			String[] tmp = name.split(" ");
			givenNameArray.add(tmp[0]);
			familyNameArray.add(tmp[1]);			
			
		}
	
		int i = 0;
		//Creazione VCard e riempimento del modello
		for(String URI : URIarray){			
			
			Resource currentPerson = model.createResource(URI);
			VCard currentVCard = new VCard();
			StructuredName tmp = new StructuredName();
			Telephone tel = new Telephone(phoneArray[i]);
			tel.getTypes().add(TelephoneType.HOME);
			tel.setPref(1);
			
			tmp.setFamily(familyNameArray.get(i));
			tmp.setGiven(givenNameArray.get(i));
			currentVCard.setFormattedName(fullNameArray[i]);
			currentVCard.setStructuredName(tmp);
			currentVCard.setUid(new Uid(URI));
			currentVCard.addTelephoneNumber(tel);
						
			vcardArray.add(currentVCard);
						
			currentPerson.addProperty(VCARD.FN, currentVCard.getFormattedName().getValue())
						 .addProperty(VCARD.TEL, currentVCard.getTelephoneNumbers().get(0).getText())
						 .addProperty(VCARD.Given, currentVCard.getStructuredName().getGiven())
						 .addProperty(VCARD.Family, currentVCard.getStructuredName().getFamily());
			ResourceArray.add(currentPerson);
			i++;
		}		
		
		StringWriter output = new StringWriter() ;
		model.write(output, "N-TRIPLES");
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Modello prima che sia importato il nuovo contatto
		//System.out.println("Before adding new contacts:\n" + output);
		
		i = 0;
		for(VCard v : vcardArray){

			try{
			    PrintWriter writer = new PrintWriter(
			    						v.getFormattedName().getValue()
			    										.replaceAll(" " , "-")
			    										.replace("\'", "-") +".html", "UTF-8");
			    //Compatibilità con h-card
			    writer.print(v.writeHtml().replace("<div class=\"vcard\">", "<div class=\"h-card vcard\">"));
			    writer.close();
			} catch (Exception e) {
			   e.printStackTrace();
			}
			++i;
		}
				
		
		//fonte da cui importare la vcard
		String source = "https://fedexist.github.io/tutorialJenaSWT/Ciccio-Pasticcio.html";
		
			try {
				String svcard = getVCF("http://h2vx.com/vcf/" + source);
				VCard vcard = Ezvcard.parse(svcard).first();
				
				//System.out.println(svcard); //Stampo il contatto VCF
				
				Resource contact = model.createResource(vcard.getUid().getValue());
				contact.addProperty(VCARD.FN, vcard.getFormattedName().getValue())
						.addProperty(VCARD.Given, vcard.getStructuredName().getGiven())
						.addProperty(VCARD.Family, vcard.getStructuredName().getFamily())
						.addProperty(VCARD.TEL, vcard.getTelephoneNumbers().get(0).getText());
				ResourceArray.add(contact);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		output = new StringWriter() ;
		model.write(output);
		
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("After adding new contacts: \n" + output);
		
		//Scrittura file RDF
		try{
		    PrintWriter writer = new PrintWriter("output.rdf", "UTF-8");
		    writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		    writer.print(output);
		    writer.close();
		} catch (Exception e) {
		   e.printStackTrace();
		}

	String queryString = " SELECT ?x WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#TEL>  \"1234567\" } " ;
	
	Query query = QueryFactory.create(queryString) ;
	
	try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		ResultSet results = qexec.execSelect() ;
		for ( ; results.hasNext() ; )
		{
			QuerySolution soln = results.nextSolution() ;
			RDFNode x = soln.get("x") ;       // Get a result variable by name.
			Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
			Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
			
			System.out.println(soln.toString());
			System.out.println(x);
		}
	}
		

	}
	

}