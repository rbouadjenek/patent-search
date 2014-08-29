package nicta.com.au.failureanalysis.utility;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.analysis.GetSectionTerms;
import nicta.com.au.main.Functions;

/**
 * @author Mona
 *
 */
public class GetPatentFile {

	public String _documentid = null;
	
	
/**
 * This method returns the hierarchical path where the patent is located.
 * @param patentID
 * @return
 * @throws IOException
 */
	public String GetPatentPath(String patentID) throws IOException {
		
		_documentid = patentID;
//		System.out.println(_documentid);
		String f1 = _documentid.split("-")[0];
		String b = _documentid.split("-")[1];
		String f2 = b.substring(0, Math.min(b.length(), 1));
		String f3 = b.substring(1, 3);
		String f4 = b.substring(3, 5);
		String f5 = b.substring(b.length() - 2);
		
		if (f2.equals("0"))
		return f1 + "/000000" + "/" + f3 + "/" + f4 + "/" + f5 + "/UN-" + _documentid + ".xml";
		else return f1 + "/000001" + "/" + f3 + "/" + f4 + "/" + f5 + "/UN-" + _documentid + ".xml";
		/*System.out.println();
		return path;*/
	}
		

	public static void main(String[] args) throws IOException {

//		String rootpath = "C:/Users/Mona/workspace/PatentSearch/data/";
		
/*------------------ I should switch between these two path when using windows machine or Linux ----------------*/	
		
//		String rootpath = "F:/";
		String rootpath = "/media/mona/MyProfesion/";

		/*GetSectionTerms query = new GetSectionTerms(
				"C:/Users/Mona/workspace/PatentSearch/data/CLEF-IP-2010/PAC_test/topics/PAC-1460_EP-1243261-A1.xml",
				0, 0, 1, 0, 0, 0, true, true);*/
		
		GetSectionTerms query = new GetSectionTerms(
				"./data/CLEF-IP-2010/PAC_test/topics/PAC-1149_EP-1724448-A2.xml"
				/*"C:/Users/Mona/workspace/PatentSearch/data/CLEF-IP-2010/PAC_test/topics/PAC-1149_EP-1724448-A2.xml"*/,
				0, 0, 1, 0, 0, 0, true, true);

//		String _patentid = /*"EP-1018339"*/ "EP-1508357" /*"EP-1666121"*/ /*"EP-1508356"*/ /*"EP-1688171"*/ /*"EP-1508355"*/ /*"EP-1775009"*/ /*"EP-1142619"*/ /*"EP-1516659"*/ /*"EP-1508358"*/ /*"EP-0816065"*/ /*"EP-1270202"*/ /*"EP-1249262"*/;
		String _patentid = "EP-0277012";
		GetPatentFile gpf = new GetPatentFile();
		String path = gpf.GetPatentPath(_patentid);
		System.out.println(path);
		

		GetSectionTerms doc = new GetSectionTerms(
				rootpath + path
				/* "C:/Users/Mona/workspace/PatentSearch/data/RelDocs/RelDocsPAC-1460/PAC-1460_EP-1243261-A1.xml" */,
				0, 0, 1, 0, 0, 0, true, true);

		Map<String, Integer> querymap = query.analyze();
		Map<String, Integer> docmap = doc.analyze();

		int docfreq;

		for (Entry<String, Integer> q : querymap.entrySet()) {

			if (!Functions.isNumeric(q.getKey())) {

				if (docmap.get(q.getKey()) != null) {
					docfreq = docmap.get(q.getKey());

				} else {
					docfreq = 0;
				}
				
				System.out.println(q.getKey() + "\t" + querymap.get(q.getKey()) + "\t" + docfreq);
			}
		}

	}
}
