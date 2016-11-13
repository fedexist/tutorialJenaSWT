package tutorialJena;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.*;

public class HelloWorld {
	
	static private String personURI = "https://root/";
	private static String fullNameArray[] = { "Federico D'Ambrosio", "Enrico Ferro", "Edoardo Ferrante", "Giulia Cagnes" };
	private static String birthdayArray[] = { "01/01/1993", "02/02/1993", "03/03/1993", "04/04/1993" };
	

	public HelloWorld() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
		
		Model model = ModelFactory.createDefaultModel();
		
		ArrayList<String> URIarray = new ArrayList<>();
		ArrayList<Resource> ResourceArray = new ArrayList<>();
		ArrayList<String> givenNameArray = new ArrayList<>();
		ArrayList<String> familyNameArray = new ArrayList<>();
				
		
		for(String name : fullNameArray){
			URIarray.add(personURI + name.replaceAll(" " , "_"));
			String[] tmp = name.split(" ");
			givenNameArray.add(tmp[0]);
			familyNameArray.add(tmp[1]);			
			
		}
	
		int i = 0;
		for(String URI : URIarray){
			Resource currentPerson = model.createResource(URI); 
			currentPerson.addProperty(VCARD.FN, fullNameArray[i])
						 .addProperty(VCARD.BDAY, birthdayArray[i])
						 .addProperty(VCARD.Given, givenNameArray.get(i))
						 .addProperty(VCARD.Family, familyNameArray.get(i));
			ResourceArray.add(currentPerson);
			i++;
		}
		
		StmtIterator iter = model.listStatements();
		
		
		String str = "";
		
		for(i=0; i< URIarray.size(); ++i){
			str+="\t<p class=\"h-card\">\n";
			str+="\t\t<span class=\"p-given-name\">"+givenNameArray.get(i)+"</span>\n";
			str+="\t\t<span class=\"p-family-name\">"+  familyNameArray.get(i) +"</span>\n";
			str+="\t\t<a class=\"p-name\" href=\"" + URIarray.get(i) + "\">"+fullNameArray[i] +"</a>\n";
			str+="\t\t<span class=\"dt-bday\">"+ birthdayArray[i] +"</span>\n";
			str+="\t</p>\n";
			
		}
		
		try{
		    PrintWriter writer = new PrintWriter("hcard.html", "UTF-8");
		    writer.println("<html>");
		    writer.println("\t<body>");
		    writer.println("\t" + str);
		    writer.println("\t</body>");
		    writer.println("</html>");
		    writer.close();
		} catch (Exception e) {
		   // do something
		}

		System.out.println(str);
		//StringWriter output = new StringWriter() ;
		
		//RDFDataMgr.write(output, model, RDFFormat.JSONLD_PRETTY);	
		//model.write(output, "N-TRIPLES");
		//System.out.println(output.toString());
	}
	

}