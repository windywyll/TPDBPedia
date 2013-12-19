import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


public class Recherche {
	
	private String query;
	
	Recherche(){
		PropertyConfigurator.configure("C:/apache-jena-2.11.0/jena-log4j.properties");
		query = "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>"+
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
				"PREFIX : <http://dbpedia.org/resource/>"+
				"PREFIX dbpedia2: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
	    		"PREFIX dbo:<http://dbpedia.org/ontology/> ";
	}
	
	public ArrayList<String[]> executeQuery(String requete, int mode)
	{
		String service = "http://dbpedia.org/sparql";
		String[] tab;
		ArrayList<String[]> sol = new ArrayList<String[]>();
		QueryExecution qe = QueryExecutionFactory.sparqlService(service, requete);
	    try {
	        ResultSet results = qe.execSelect();
	        if(results != null)
	        {
		        for (; results.hasNext();) {
		        		//if(mode != 0)
		        	tab = new String[3];
		        		QuerySolution q = (QuerySolution) results.next();
		        		
		        		if(q.get("artistName")!=null)
		        			tab[0] = q.get("artistName").toString();
		        		if(q.get("albumName")!=null)
		        			tab[1] = q.get("albumName").toString();
		        		if(q.get("genreName")!=null)
		        			tab[2] = q.get("genreName").toString();
		        		//System.out.println(tab[0] +" "+tab[1] +" "+tab[2]);
		        		sol.add(tab);
		        		//else
		        			
			            //System.out.println(sol.get("?name"));
			        }
	        }
	    }catch(Exception e){

	        e.printStackTrace();
	    }
	    finally {

	       qe.close();
	    }
		return sol;
	}
	
	public ArrayList<String[]> rechercheArtiste(String artist, String genre, int offset){
		query = query +
				"PREFIX agent: <http://dbpedia.org/ontology/Agent>"+
				"PREFIX band: <http://dbpedia.org/ontology/Band>"+
				"PREFIX artist: <http://dbpedia.org/ontology/MusicalArtist>"+
				
				"SELECT distinct ?artist ?artistName ?artistGenre WHERE {"+
				"?artist rdf:type agent:."+
				"?artist rdfs:subClassOf* ?subclass."+
				"?subclass rdf:type ?subType."+
				"FILTER ((?subType= band:) || (?subType= artist:))."+
				"?artist rdfs:label ?artistName."+
				"FILTER(lang(?artistName) = 'en')."+
				"?artist dbpedia2:genre ?artistGenre.";
		if(genre != null)
		{
			query = query + "FILTER regex(?artistGenre,'(ressource/)?.*"+genre+".*','i')";
		}
				query = query + "FILTER (regex(?artist, 'resource/.*"+artist+".*', 'i'))."+
				"}ORDER BY ?artistName "+
				"OFFSET "+offset+
		 		"LIMIT 100";
		
		
		return executeQuery(query, 1);
	}
	
	public ArrayList<String[]> rechercheAlbum(String album, String artist, String genre, int offset){
		
		query = query +
				"PREFIX album: <http://dbpedia.org/ontology/MusicalWork> "+
				
				"SELECT distinct ?album ?name ?artist WHERE {"+
				"?album rdf:type album:."+
				"?album rdfs:label ?name."+
				"FILTER(lang(?name) = 'en')."+
				"?album dbpedia2:artist ?artist."+
				//"FILTER(?released < '1985-01-01'^^xsd:date)."+
				"FILTER regex(str(?artist), '"+album+"', 'i')."+
				"}ORDER BY ?artist "+
				"OFFSET "+offset+
				"LIMIT 100";
		 
		return executeQuery(query,2);
	}

	public ArrayList<String[]> rechercheGenre(String genre, int offset){
		query = query +
				"PREFIX genre: <http://dbpedia.org/ontology/MusicGenre> "+
				
			 	"SELECT distinct ?genreName"+
				"WHERE {"+
				"?genre rdf:type genre:."+
				"?genre dbpedia2:name ?genreName."+
				"FILTER regex(str(?genreName), "+genre+", 'i')."+
				"}ORDER BY ?genreName "+
				"OFFSET "+offset+
				"LIMIT 100";
	
		return executeQuery(query,3);
	}
	
	public ArrayList<String[]> rechercheGlobale(String objetRecherche){
		query = query +
				"PREFIX artist: <http://dbpedia.org/ontology/Band> "+
				"PREFIX album: <http://dbpedia.org/ontology/MusicalWork> "+
				"PREFIX genre: <http://dbpedia.org/ontology/MusicGenre> "+
				
				"SELECT ?albumName ?genreName ?artistName WHERE {"+
				"{?genre rdf:type <http://dbpedia.org/ontology/MusicGenre>. "+
				" ?genre rdfs:label ?genreName. "+
				"FILTER (lang(?genreName) = 'en')."+
				"FILTER regex(str(?genreName), '"+objetRecherche+"','i').} UNION "+
				
				"{?artist rdf:type <http://dbpedia.org/ontology/Band>. "+
				"?artist rdfs:label ?artistName. "+
				"FILTER (lang(?artistName) = 'en')."+
				"FILTER regex(str(?artist), '"+objetRecherche+"','i').} UNION "+
				
				"{?album rdf:type <http://dbpedia.org/ontology/MusicalWork>. "+
				"?album rdfs:label ?albumName. "+
				"FILTER (lang(?albumName) = 'en')."+
				"FILTER regex(str(?album), '"+objetRecherche+"','i').}"+
				"} order by ?genreName "+
				"limit 500";
		
		
		return executeQuery(query,0);
	}
	
}
