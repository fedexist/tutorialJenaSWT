package tutorialJena;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Birthday;
import ezvcard.property.StructuredName;
import ezvcard.property.Uid;

public class HelloWorld {
	
	
	
	static private String personURI = "https://fleanend.github.io/";
	private static String fullNameArray[] = { "Federico D'Ambrosio", "Enrico Ferro", "Edoardo Ferrante", "Giulia Cagnes" };
	private static String birthdayArray[] = {"1993-01-01", "1993-02-02", "1993-03-03", "1993-04-04" };
	

	public HelloWorld() {
		// TODO Auto-generated constructor stub
	}
	
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
				
		for(String name : fullNameArray){
			URIarray.add(personURI + name.replaceAll(" " , "_").replace("\'", "%27"));
			String[] tmp = name.split(" ");
			givenNameArray.add(tmp[0]);
			familyNameArray.add(tmp[1]);			
			
		}
	
		int i = 0;
		for(String URI : URIarray){
			
			c.clear();
			
			String tmp1[] = birthdayArray[i].split("-");
			
			c.set(Calendar.YEAR, Integer.parseInt(tmp1[0]));
			c.set(Calendar.MONTH, Integer.parseInt(tmp1[1]) - 1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tmp1[2]));
			
			Resource currentPerson = model.createResource(URI);
			VCard currentVCard = new VCard();
			StructuredName tmp = new StructuredName();
			Birthday tmpbDay = new Birthday(c.getTime());
			
			tmp.setFamily(familyNameArray.get(i));
			tmp.setGiven(givenNameArray.get(i));
			currentVCard.setFormattedName(fullNameArray[i]);
			currentVCard.setStructuredName(tmp);
			currentVCard.setBirthday(tmpbDay);
			currentVCard.setUid(new Uid(URI));
						
			vcardArray.add(currentVCard);
			
			currentPerson.addProperty(VCARD.FN, currentVCard.getFormattedName().getValue())
						 .addProperty(VCARD.BDAY, birthdayArray[i])
						 .addProperty(VCARD.Given, currentVCard.getStructuredName().getGiven())
						 .addProperty(VCARD.Family, currentVCard.getStructuredName().getFamily());
			ResourceArray.add(currentPerson);
			i++;
		}		
		
		StringWriter output = new StringWriter() ;
		model.write(output, "N-TRIPLES");
		System.out.println(output);
		
		i = 0;
		for(VCard v : vcardArray){

			try{
			    PrintWriter writer = new PrintWriter(
			    						v.getFormattedName().getValue()
			    										.replaceAll(" " , "_")
			    										.replace("\'", "%27") +".html", "UTF-8");

			    writer.print(v.writeHtml().replace("<div class=\"vcard\">", "<div class=\"h-card vcard\">"));
			    writer.close();
			} catch (Exception e) {
			   e.printStackTrace();
			}
			++i;
		}
				
		//System.out.println(str);

		//System.out.println("Before adding contacts: \n" + output.toString());
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
		
		if(args.length != 0){
			try {
				String svcard = getVCF("http://h2vx.com/vcf/" + args[0]+ "hcard_0.html");
				VCard vcard = Ezvcard.parse(svcard).first();
				//System.out.println(vcard);
				//VCard vcard = Ezvcard.parseHtml(args[0] + "hcard_0.html").;
				//VCard vcard = Ezvcard.parse(getVCF("http://h2vx.com/vcf/" + args[0])).first();
				//System.out.println(getVCF("http://h2vx.com/vcf/" + args[0]));
				Resource contact = model.createResource(vcard.getUid().toString());
				contact.addProperty(VCARD.FN, vcard.getFormattedName().getValue())
						.addProperty(VCARD.Given, vcard.getStructuredName().getGiven())
						.addProperty(VCARD.Family, vcard.getStructuredName().getFamily())
						.addProperty(VCARD.BDAY, df.format(vcard.getBirthday().getDate()));
				ResourceArray.add(contact);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//model.write(output, "N-TRIPLES");
		//System.out.println("After adding new contacts: \n" + output.toString());

	}
	

}